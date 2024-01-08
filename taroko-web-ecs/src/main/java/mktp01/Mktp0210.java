/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-29  V1.00.00  yash       program initial                            *
* 109-01-06  V1.00.01   Justin Wu    updated for archit.  change
* 109-04-28  V1.00.02  YangFang   updated for project coding standard 
* 110-09-07  V1.00.03  Machao     新增POS終端機代號設定按钮       
* 111-02-28  V1.00.04  machao       页面调整       
* 111-03-22  V1.00.05  machao       新增aud_type栏，逻辑处理*
* 112-08-04  V1.00.06  Zuwei Su   覆核reward_type欄位超長                 
* 112-09-05  V1.00.07  machao     覆核失败调整          *
******************************************************************************/

package mktp01;

import busi.SqlPrepare;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Mktp0210 extends BaseProc {
  String mExGroupCode = "";
  String mProgramCode = "";
  String mMaiTable = "";
  String mGroupCode = "";
  int lsCt1 = 0, lsCt2 = 0, lsCt3 = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "R":
        // * 查詢功能 */
        strAction = "R";
        querySelect();
        break;
      case "R1":
        /* 查詢功能 */
        strAction = "R1";
        querySelect();
        break;
      case "R2":
        /* 查詢功能 */
        strAction = "R2";
        querySelect();
        break;
      case "R3":
    	  /*pos终端机*/
    	 strAction = "R3";
    	 querySelect();
    	 break;
      case "A":
        /* 新增功能 */
        // insertFunc();
        break;
      case "U":
        /* 更新功能 */
        // updateFunc();
        break;
      case "D":
        /* 刪除功能 */
        // deleteFunc();
        break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      case "S2":
        /* 執行 */
        strAction = "S2";
        dataProcess();
        break;
      // case "AJAX":
      // // AJAX 20200106 updated for archit. change
      //
      // break;
      default:
        break;
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {

    wp.whereStr = " where 1=1  and apr_flag <> 'Y' ";

    if (empty(wp.itemStr("ex_user")) == false) {
      wp.whereStr += " and  crt_user = :ex_user ";
      setString("ex_user", wp.itemStr("ex_user"));
    }

    if (empty(wp.itemStr("ex_acct_date1")) == false) {
      wp.whereStr += " and  crt_date >= :ex_acct_date1 ";
      setString("ex_acct_date1", wp.itemStr("ex_acct_date1"));
    }

    if (empty(wp.itemStr("ex_acct_date2")) == false) {
      wp.whereStr += " and  crt_date <= :ex_acct_date2 ";
      setString("ex_acct_date2", wp.itemStr("ex_acct_date2"));
    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "group_code," + " description        ," 
    	+" reward_type ,"
        +" DECODE ( reward_type,'0','0.不產生回饋', '1',  '1.按消費金額','2', '2.依店內店外消費金額')  as reward_type_desc ,"
        + " base_amt_1         ," + " purch_amt_s_a1     ," + " purch_amt_e_a1     ,"
        + " rate_a1            ," + " purch_amt_s_a2     ," + " purch_amt_e_a2     ,"
        + " rate_a2            ," + " purch_amt_s_a3     ," + " purch_amt_e_a3     ,"
        + " rate_a3            ," + " purch_amt_s_a4     ," + " purch_amt_e_a4     ,"
        + " rate_a4            ," + " purch_amt_s_a5     ," + " purch_amt_e_a5     ,"
        + " rate_a5            ," + " mcht_no_1          ," + " mcht_no_2          ,"
        + " mcht_no_3          ," + " mcht_no_4          ," + " mcht_no_5          ,"
        + " mcht_no_6          ," + " mcht_no_7          ," + " mcht_no_8          ,"
        + " mcht_no_9          ," + " mcht_no_10         ," + " int_amt_s_1        ,"
        + " int_amt_e_1        ," + " int_rate_1         ," + " int_amt_s_2        ,"
        + " int_amt_e_2        ," + " int_rate_2         ," + " int_amt_s_3        ,"
        + " int_amt_e_3        ," + " int_rate_3         ," + " int_amt_s_4        ,"
        + " int_amt_e_4        ," + " int_rate_4         ," + " int_amt_s_5        ,"
        + " int_amt_e_5        ," + " int_rate_5         ," + " out_amt_s_1        ,"
        + " out_amt_e_1        ," + " out_rate_1         ," + " out_amt_s_2        ,"
        + " out_amt_e_2        ," + " out_rate_2         ," + " out_amt_s_3        ,"
        + " out_amt_e_3        ," + " out_rate_3         ," + " out_amt_s_4        ,"
        + " out_amt_e_4        ," + " out_rate_4         ," + " out_amt_s_5        ,"
        + " out_amt_e_5        ," + " out_rate_5         ," + " item_ename_bl      ,"
        + " item_ename_bl_in   ," + " item_ename_bl_out  ," + " item_ename_it      ,"
        + " item_ename_it_in   ," + " item_ename_it_out  ," + " item_ename_ca      ,"
        + " item_ename_id      ," + " item_ename_ao      ," + " item_ename_ot      ,"
        + " present_type       ," + " rate_a12           ," + " rate_a22           ,"
        + " rate_a32           ," + " rate_a42           ," + " rate_a52           ,"
        + " int_rate_12        ," + " int_rate_22        ," + " int_rate_32        ,"
        + " int_rate_42        ," + " int_rate_52        ," + " out_rate_12        ,"
        + " out_rate_22        ," + " out_rate_32        ," + " out_rate_42        ,"
        + " out_rate_52        ," + " program_code       ," + " purch_date_type    ,"
        + " run_time_dd        ," + " crt_user           ," + " crt_date           ,"
        + " apr_flag           ," + " apr_user           ," + " apr_date           ,"
        + " aud_type			";

    
    wp.daoTable = "mkt_purc_gp_t";
    wp.whereOrder = " order by crt_date";
    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    commGroupCode("comm_group_code");
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
	  int sel_ct = wp.selectCnt;
		for (int ii = 0; ii < sel_ct; ii++) {
			String ss = wp.colStr(ii, "aud_type");
			String[] cde = new String[] { "Y","A","U","D" };
			String[] txt = new String[] { "未異動","新增待覆核","更新待覆核","刪除待覆核" };
			wp.colSet(ii, "commfunc_aud_type", commString.decode(ss, cde, txt));
		}
  }

  @Override
  public void querySelect() throws Exception {
    if (strAction.equals("R")) {
      dataRead();
    }
    if (strAction.equals("R1")) {
      dataRead1();
    }
    if (strAction.equals("R2")) {
      dataRead2();
    }
    if (strAction.equals("R3")) {
        dataRead3();
    }
  }

  @Override
  public void dataRead() throws Exception {
    mExGroupCode = itemKk("kk_group_code");
    mProgramCode = itemKk("data_k2");
    if (empty(mExGroupCode)) {
		mExGroupCode = itemKk("data_k1");
	}
    mMaiTable = "mkt_purc_gp_t";

    wp.selectSQL = "hex(rowid) as rowid, " + "group_code, " + "description, " + "reward_type, "
        + "base_amt_1, " + "purch_amt_s_a1, " + "purch_amt_e_a1, " + "rate_a1, "
        + "purch_amt_s_a2, " + "purch_amt_e_a2, " + "rate_a2, " + "purch_amt_s_a3, "
        + "purch_amt_e_a3, " + "rate_a3, " + "purch_amt_s_a4, " + "purch_amt_e_a4, " + "rate_a4, "
        + "purch_amt_s_a5, " + "purch_amt_e_a5, " + "rate_a5, " + "mcht_no_1, " + "mcht_no_2, "
        + "mcht_no_3, " + "mcht_no_4, " + "mcht_no_5, " + "mcht_no_6, " + "mcht_no_7, "
        + "mcht_no_8, " + "mcht_no_9, " + "mcht_no_10, " + "int_amt_s_1, " + "int_amt_e_1, "
        + "int_rate_1, " + "int_amt_s_2, " + "int_amt_e_2, " + "int_rate_2, " + "int_amt_s_3, "
        + "int_amt_e_3, " + "int_rate_3, " + "int_amt_s_4, " + "int_amt_e_4, " + "int_rate_4, "
        + "int_amt_s_5, " + "int_amt_e_5, " + "int_rate_5, " + "out_amt_s_1, " + "out_amt_e_1, "
        + "out_rate_1, " + "out_amt_s_2, " + "out_amt_e_2, " + "out_rate_2, " + "out_amt_s_3, "
        + "out_amt_e_3, " + "out_rate_3, " + "out_amt_s_4, " + "out_amt_e_4, " + "out_rate_4, "
        + "out_amt_s_5, " + "out_amt_e_5, " + "out_rate_5, " + "item_ename_bl, "
        + "item_ename_bl_in, " + "item_ename_bl_out, " + "item_ename_it, " + "item_ename_it_in, "
        + "item_ename_it_out, " + "item_ename_ca, " + "item_ename_id, " + "item_ename_ao, "
        + "item_ename_ot, " + "present_type, " + "rate_a12, " + "rate_a22, " + "rate_a32, "
        + "rate_a42, " + "rate_a52, " + "int_rate_12, " + "int_rate_22, " + "int_rate_32, "
        + "int_rate_42, " + "int_rate_52, " + "out_rate_12, " + "out_rate_22, " + "out_rate_32, "
        + "out_rate_42, " + "out_rate_52, " + "program_code, " + "purch_date_type, "
        + "run_time_dd, " + "crt_user, " + "crt_date, " + "apr_flag, " + "apr_user, " + "apr_date ," + "aud_type ";
//    if (mMaiTable.equals("mkt_purc_gp_t")) {
//      wp.selectSQL += ", 'U.更新待覆核' as app_flag";
//    } else if (mMaiTable.equals("mkt_purc_gp")) {
//      wp.selectSQL += ", 'Y.未異動' as app_flag";
//
//    }
    wp.daoTable = mMaiTable;
    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and  group_code = :group_code ";
    setString("group_code", mExGroupCode);

    // System.out.println("group_code : "+m_ex_group_code);
    // System.out.println("select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);

    pageSelect();
    if (mMaiTable.equals("mkt_purc_gp")) {
      wp.colSet("d_disable", "disabled style='background-color: lightgray;'");
    }

    if (sqlNotFind()) {
      alertErr("查無資料, group_code =" + mExGroupCode);
    }
//     list_wkdata();
    commGroupCode("comm_group_code");
  }

  @Override
  public void dddwSelect() {
	  try {
	        // dddw_group_code
	        wp.initOption = "--";
	        wp.optionKey = itemKk("ex_user");
	        dddwList("dddw_sec_user", "sec_user", "usr_id","usr_cname",
	        		"where 1=1 order by usr_id");
	      } catch (Exception ex) {
	      }

	  
    if (wp.respHtml.indexOf("_detl") > 0) {
      try {
        // dddw_group_code
        wp.initOption = "--";
        wp.optionKey = itemKk("kk_group_code");
        dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name",
            "where 1=1 group by group_code,group_name order by group_code");
      } catch (Exception ex) {
      }

    }
    if (wp.respHtml.indexOf("_mcht_group") > 0) {
      try {
        // dddw_mcht_group
        wp.initOption = "--";
        wp.optionKey = itemKk("dddw_mcht_group");
        dddwList("dddw_mcht_group", "mkt_mcht_gp", "mcht_group_id", "mcht_group_desc",
            "where 1=1 order by mcht_group_id");

      } catch (Exception ex) {
      }
    }
    if (wp.respHtml.indexOf("_mcht_group_out") > 0) {
      try {
        // dddw_mcht_group
        wp.initOption = "--";
        wp.optionKey = itemKk("dddw_mcht_group");
        dddwList("dddw_mcht_group", "mkt_mcht_gp", "mcht_group_id", "mcht_group_desc",
            "where 1=1 order by mcht_group_id");

      } catch (Exception ex) {
      }
    }
  }
  
  public void dataRead1() throws Exception {
    this.selectNoLimit();
    mProgramCode = wp.itemStr("program_code");
    if (empty(mProgramCode)) {
      mProgramCode = itemKk("data_m1");
    }
    if (empty(mProgramCode)) {
      mProgramCode = wp.itemStr("kk_program_code");
    }
    wp.colSet("kk_program_code", mProgramCode);

    if (empty(mProgramCode)) {
      alertErr("請先點選團體代號!!");
      return;
    }
    wp.selectSQL = "hex(rowid) as rowid1, " + "program_code , " + "data_type , " + "data_code ";
    wp.daoTable = "ptr_bn_data_t ";
    wp.whereStr = "where 1=1 and data_type='2' ";
    wp.whereStr += "and program_code =:program_code ";
    setString("program_code", mProgramCode);

    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";
    if (sqlNotFind()) {
      alertErr("查無排除特店資料");
    }
    wp.colSet("row_ct", wp.selectCnt);
  }

  public void dataRead2() throws Exception {
    this.selectNoLimit();
    mGroupCode = wp.itemStr("group_code");
    if (empty(mGroupCode)) {
      mGroupCode = itemKk("data_n1");
    }
    wp.colSet("group_code", mGroupCode);
    if (empty(mGroupCode)) {
      alertErr("請先點選團體代號!!");
      return;
    }
    commGroupCode("comm_group_code");
    wp.selectSQL = "hex(rowid) as rowid2, " + "GROUP_CODE , " + "MCHT_GROUP_ID as data_code ";
    wp.daoTable = "mkt_purcgp_ext_t ";
    wp.whereStr = "where 1=1 and group_code=:group_code ";
    setString("group_code", mGroupCode);
    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";
    lsCt2 = wp.selectCnt;
    wp.colSet("row_ct", lsCt2);
    
  }

  public void dataRead3() throws Exception{
	  this.selectNoLimit();
		if (wp.itemStr("ex_apr_flag").equals("Y") || wp.itemStr("apr_flag").equals("Y")) {
			mMaiTable = "ptr_bn_data";
		} else {
			mMaiTable = "ptr_bn_data_t";
		}
		mProgramCode = wp.itemStr("program_code");
		if (empty(mProgramCode)) {
			mProgramCode = itemKk("data_m1");
		}

		if (empty(mProgramCode)) {
			alertErr("請先點選團體代號!!");
			return;
		}
		 commGroupCode("comm_group_code");
		wp.selectSQL = "hex(rowid) as rowid1, " + "program_code , " + "data_type , " + "data_code ";
		wp.daoTable = mMaiTable;
		wp.whereStr = "where 1=1 and data_type='5' ";
		wp.whereStr += "and program_code =:program_code ";
		setString("program_code", mProgramCode);

		pageQuery();
		wp.setListCount(1);
		wp.notFound = "";
		if (sqlNotFind()) {
			alertErr("查無排除特店資料");
		}
		wp.colSet("row_ct", wp.selectCnt);
		wp.colSet("program_code", mProgramCode);

  }
  
  @Override
  public void dataProcess() throws Exception {

    String[] opt = wp.itemBuff("opt");
    String[] aaGroupCode = wp.itemBuff("group_code");
    String[] aaDescription = wp.itemBuff("description");
    String[] aaRewardType = wp.itemBuff("Reward_type");
    String[] aaBaseAmt1 = wp.itemBuff("base_amt_1");
    String[] aaPurchAmtSA1 = wp.itemBuff("purch_amt_s_a1");
    String[] aaPurchAmtEA1 = wp.itemBuff("purch_amt_e_a1");
    String[] aaRateA1 = wp.itemBuff("rate_a1");
    String[] aaPurchAmtSA2 = wp.itemBuff("purch_amt_s_a2");
    String[] aaPurchAmtEA2 = wp.itemBuff("purch_amt_e_a2");
    String[] aaRateA2 = wp.itemBuff("rate_a2");
    String[] aaPurchAmtSA3 = wp.itemBuff("purch_amt_s_a3");
    String[] aaPurchAmtEA3 = wp.itemBuff("purch_amt_e_a3");
    String[] aaRateA3 = wp.itemBuff("rate_a3");
    String[] aaPurchAmtSA4 = wp.itemBuff("purch_amt_s_a4");
    String[] aaPurchAmtEA4 = wp.itemBuff("purch_amt_e_a4");
    String[] aaRateA4 = wp.itemBuff("rate_a4");
    String[] aaPurchAmtSA5 = wp.itemBuff("purch_amt_s_a5");
    String[] aaPurchAmtEA5 = wp.itemBuff("purch_amt_e_a5");
    String[] aaRateA5 = wp.itemBuff("rate_a5");
    String[] aaMchtNo1 = wp.itemBuff("mcht_no_1");
    String[] aaMchtNo2 = wp.itemBuff("mcht_no_2");
    String[] aaMchtNo3 = wp.itemBuff("mcht_no_3");
    String[] aaNchtno4 = wp.itemBuff("mcht_no_4");
    String[] aaMchtNo5 = wp.itemBuff("mcht_no_5");
    String[] aaMchtNo6 = wp.itemBuff("mcht_no_6");
    String[] aaMchtNo7 = wp.itemBuff("mcht_no_7");
    String[] aaMchtNo8 = wp.itemBuff("mcht_no_8");
    String[] aaMchtNo9 = wp.itemBuff("mcht_no_9");
    String[] aaMchtNo10 = wp.itemBuff("mcht_no_10");
    String[] aaIntAmtS1 = wp.itemBuff("int_amt_s_1");
    String[] aaIntAmtE1 = wp.itemBuff("int_amt_e_1");
    String[] aaIntRate1 = wp.itemBuff("int_rate_1");
    String[] aaIntAmtS2 = wp.itemBuff("int_amt_s_2");
    String[] aaIntAmtE2 = wp.itemBuff("int_amt_e_2");
    String[] aaIntRate2 = wp.itemBuff("int_rate_2");
    String[] aaIntAmtS3 = wp.itemBuff("int_amt_s_3");
    String[] aaIntAmtE3 = wp.itemBuff("int_amt_e_3");
    String[] aaIntRate3 = wp.itemBuff("int_rate_3");
    String[] aaIntAmtS4 = wp.itemBuff("int_amt_s_4");
    String[] aaIntAmtE4 = wp.itemBuff("int_amt_e_4");
    String[] aaIntRate4 = wp.itemBuff("int_rate_4");
    String[] aaIntAmtS5 = wp.itemBuff("int_amt_s_5");
    String[] aaIntAmtE5 = wp.itemBuff("int_amt_e_5");
    String[] aaIntRate5 = wp.itemBuff("int_rate_5");
    String[] aaOutAmtS1 = wp.itemBuff("out_amt_s_1");
    String[] aaOutAmtE1 = wp.itemBuff("out_amt_e_1");
    String[] aaOutRate1 = wp.itemBuff("out_rate_1");
    String[] aaOutAmtS2 = wp.itemBuff("out_amt_s_2");
    String[] aaOutAmtE2 = wp.itemBuff("out_amt_e_2");
    String[] aaOutRate2 = wp.itemBuff("out_rate_2");
    String[] aaOutAmtS3 = wp.itemBuff("out_amt_s_3");
    String[] aaOutAmtE3 = wp.itemBuff("out_amt_e_3");
    String[] aaOutRate3 = wp.itemBuff("out_rate_3");
    String[] aaOutAmtS4 = wp.itemBuff("out_amt_s_4");
    String[] aaOutAmtE4 = wp.itemBuff("out_amt_e_4");
    String[] aaOutRate4 = wp.itemBuff("out_rate_4");
    String[] aaOutAmtS5 = wp.itemBuff("out_amt_s_5");
    String[] aaOutAmtE5 = wp.itemBuff("out_amt_e_5");
    String[] aaOutRate5 = wp.itemBuff("out_rate_5");
    String[] aaItemEnameBl = wp.itemBuff("item_ename_bl");
    String[] aaItemEnameBlIn = wp.itemBuff("item_ename_bl_in");
    String[] aaItemEnameBlOut = wp.itemBuff("item_ename_bl_out");
    String[] aaItemEnameIt = wp.itemBuff("item_ename_it");
    String[] aaItemEnameItIn = wp.itemBuff("item_ename_it_in");
    String[] aaItemEnameItOut = wp.itemBuff("item_ename_it_out");
    String[] aaItemEnameCa = wp.itemBuff("item_ename_ca");
    String[] aaItemEnameId = wp.itemBuff("item_ename_id");
    String[] aaItemEnameAo = wp.itemBuff("item_ename_ao");
    String[] aaItemEnameOt = wp.itemBuff("item_ename_ot");
    String[] aaPresentType = wp.itemBuff("present_type");
    String[] aaRateA12 = wp.itemBuff("rate_a12");
    String[] aaRateA22 = wp.itemBuff("rate_a22");
    String[] aaRateA32 = wp.itemBuff("rate_a32");
    String[] aaRateA42 = wp.itemBuff("rate_a42");
    String[] aaRateA52 = wp.itemBuff("rate_a52");
    String[] aaIntRate12 = wp.itemBuff("int_rate_12");
    String[] aaIntRate22 = wp.itemBuff("int_rate_22");
    String[] aaIntRate32 = wp.itemBuff("int_rate_32");
    String[] aaIntRate42 = wp.itemBuff("int_rate_42");
    String[] aaIntRate52 = wp.itemBuff("int_rate_52");
    String[] aaOutRate12 = wp.itemBuff("out_rate_12");
    String[] aaOutRate22 = wp.itemBuff("out_rate_22");
    String[] aaOutRate32 = wp.itemBuff("out_rate_32");
    String[] aaOutRate42 = wp.itemBuff("out_rate_42");
    String[] aaOutRate52 = wp.itemBuff("out_rate_52");
    String[] aaProgramCode = wp.itemBuff("program_code");
    String[] aaPurchDateType = wp.itemBuff("purch_date_type");
    String[] aaRunTimeDd = wp.itemBuff("run_time_dd");
    String[] aaAudType = wp.itemBuff("aud_type");

    String dsSql = "", isSql = "";
    wp.listCount[0] = aaGroupCode.length;

    // save
    int rr = -1;
    int llOk = 0, llErr = 0;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
        continue;
      }
      wp.colSet(rr, "ok_flag", "");
      if( !aaAudType[rr].equals("D")) {
			String lsSql = " select * from mkt_purc_gp where group_code=:group_code ";
			setString("group_code", aaGroupCode[rr]);
			sqlSelect(lsSql);

      if (sqlRowNum > 0) {
        // up
        busi.SqlPrepare spu = new SqlPrepare();
        spu.sql2Update("mkt_purc_gp");
        spu.ppstr("description", aaDescription[rr]);
        spu.ppstr("reward_type", aaRewardType[rr]);
        spu.ppnum("base_amt_1", toNum(aaBaseAmt1[rr]));
        spu.ppnum("purch_amt_s_a1", toNum(aaPurchAmtSA1[rr]));
        spu.ppnum("purch_amt_e_a1", toNum(aaPurchAmtEA1[rr]));
        spu.ppnum("rate_a1", toNum(aaRateA1[rr]));
        spu.ppnum("purch_amt_s_a2", toNum(aaPurchAmtSA2[rr]));
        spu.ppnum("purch_amt_e_a2", toNum(aaPurchAmtEA2[rr]));
        spu.ppnum("rate_a2", toNum(aaRateA2[rr]));
        spu.ppnum("purch_amt_s_a3", toNum(aaPurchAmtSA3[rr]));
        spu.ppnum("purch_amt_e_a3", toNum(aaPurchAmtEA3[rr]));
        spu.ppnum("rate_a3", toNum(aaRateA3[rr]));
        spu.ppnum("purch_amt_s_a4", toNum(aaPurchAmtSA4[rr]));
        spu.ppnum("purch_amt_e_a4", toNum(aaPurchAmtEA4[rr]));
        spu.ppnum("rate_a4", toNum(aaRateA4[rr]));
        spu.ppnum("purch_amt_s_a5", toNum(aaPurchAmtSA5[rr]));
        spu.ppnum("purch_amt_e_a5", toNum(aaPurchAmtEA5[rr]));
        spu.ppnum("rate_a5", toNum(aaRateA5[rr]));
        spu.ppstr("mcht_no_1", aaMchtNo1[rr]);
        spu.ppstr("mcht_no_2", aaMchtNo2[rr]);
        spu.ppstr("mcht_no_3", aaMchtNo3[rr]);
        spu.ppstr("mcht_no_4", aaNchtno4[rr]);
        spu.ppstr("mcht_no_5", aaMchtNo5[rr]);
        spu.ppstr("mcht_no_6", aaMchtNo6[rr]);
        spu.ppstr("mcht_no_7", aaMchtNo7[rr]);
        spu.ppstr("mcht_no_8", aaMchtNo8[rr]);
        spu.ppstr("mcht_no_9", aaMchtNo9[rr]);
        spu.ppstr("mcht_no_10", aaMchtNo10[rr]);
        spu.ppnum("int_amt_s_1", toNum(aaIntAmtS1[rr]));
        spu.ppnum("int_amt_e_1", toNum(aaIntAmtE1[rr]));
        spu.ppnum("int_rate_1", toNum(aaIntRate1[rr]));
        spu.ppnum("int_amt_s_2", toNum(aaIntAmtS2[rr]));
        spu.ppnum("int_amt_e_2", toNum(aaIntAmtE2[rr]));
        spu.ppnum("int_rate_2", toNum(aaIntRate2[rr]));
        spu.ppnum("int_amt_s_3", toNum(aaIntAmtS3[rr]));
        spu.ppnum("int_amt_e_3", toNum(aaIntAmtE3[rr]));
        spu.ppnum("int_rate_3", toNum(aaIntRate3[rr]));
        spu.ppnum("int_amt_s_4", toNum(aaIntAmtS4[rr]));
        spu.ppnum("int_amt_e_4", toNum(aaIntAmtE4[rr]));
        spu.ppnum("int_rate_4", toNum(aaIntRate4[rr]));
        spu.ppnum("int_amt_s_5", toNum(aaIntAmtS5[rr]));
        spu.ppnum("int_amt_e_5", toNum(aaIntAmtE5[rr]));
        spu.ppnum("int_rate_5", toNum(aaIntRate5[rr]));
        spu.ppnum("out_amt_s_1", toNum(aaOutAmtS1[rr]));
        spu.ppnum("out_amt_e_1", toNum(aaOutAmtE1[rr]));
        spu.ppnum("out_rate_1", toNum(aaOutRate1[rr]));
        spu.ppnum("out_amt_s_2", toNum(aaOutAmtS2[rr]));
        spu.ppnum("out_amt_e_2", toNum(aaOutAmtE2[rr]));
        spu.ppnum("out_rate_2", toNum(aaOutRate2[rr]));
        spu.ppnum("out_amt_s_3", toNum(aaOutAmtS3[rr]));
        spu.ppnum("out_amt_e_3", toNum(aaOutAmtE3[rr]));
        spu.ppnum("out_rate_3", toNum(aaOutRate3[rr]));
        spu.ppnum("out_amt_s_4", toNum(aaOutAmtS4[rr]));
        spu.ppnum("out_amt_e_4", toNum(aaOutAmtE4[rr]));
        spu.ppnum("out_rate_4", toNum(aaOutRate4[rr]));
        spu.ppnum("out_amt_s_5", toNum(aaOutAmtS5[rr]));
        spu.ppnum("out_amt_e_5", toNum(aaOutAmtE5[rr]));
        spu.ppnum("out_rate_5", toNum(aaOutRate5[rr]));
        spu.ppstr("item_ename_bl", aaItemEnameBl[rr]);
        spu.ppstr("item_ename_bl_in", aaItemEnameBlIn[rr]);
        spu.ppstr("item_ename_bl_out", aaItemEnameBlOut[rr]);
        spu.ppstr("item_ename_it", aaItemEnameIt[rr]);
        spu.ppstr("item_ename_it_in", aaItemEnameItIn[rr]);
        spu.ppstr("item_ename_it_out", aaItemEnameItOut[rr]);
        spu.ppstr("item_ename_ca", aaItemEnameCa[rr]);
        spu.ppstr("item_ename_id", aaItemEnameId[rr]);
        spu.ppstr("item_ename_ao", aaItemEnameAo[rr]);
        spu.ppstr("item_ename_ot", aaItemEnameOt[rr]);
        spu.ppstr("present_type", aaPresentType[rr]);
        spu.ppnum("rate_a12", toNum(aaRateA12[rr]));
        spu.ppnum("rate_a22", toNum(aaRateA22[rr]));
        spu.ppnum("rate_a32", toNum(aaRateA32[rr]));
        spu.ppnum("rate_a42", toNum(aaRateA42[rr]));
        spu.ppnum("rate_a52", toNum(aaRateA52[rr]));
        spu.ppnum("int_rate_12", toNum(aaIntRate12[rr]));
        spu.ppnum("int_rate_22", toNum(aaIntRate22[rr]));
        spu.ppnum("int_rate_32", toNum(aaIntRate32[rr]));
        spu.ppnum("int_rate_42", toNum(aaIntRate42[rr]));
        spu.ppnum("int_rate_52", toNum(aaIntRate52[rr]));
        spu.ppnum("out_rate_12", toNum(aaOutRate12[rr]));
        spu.ppnum("out_rate_22", toNum(aaOutRate22[rr]));
        spu.ppnum("out_rate_32", toNum(aaOutRate32[rr]));
        spu.ppnum("out_rate_42", toNum(aaOutRate42[rr]));
        spu.ppnum("out_rate_52", toNum(aaOutRate52[rr]));
        spu.ppstr("program_code", aaProgramCode[rr]);
        spu.ppstr("purch_date_type", aaPurchDateType[rr]);
        spu.ppnum("run_time_dd", toInt(aaRunTimeDd[rr]));
        spu.ppstr("apr_flag", "Y");
        spu.ppstr("apr_date", getSysDate());
        spu.ppstr("apr_user", wp.loginUser);
        spu.ppstr("mod_user", wp.loginUser);
        spu.ppstr("mod_pgm", wp.modPgm());
        spu.addsql(", mod_seqno =nvl(mod_seqno,0)+1 ", "");
        spu.sql2Where(" where group_code =? ", aaGroupCode[rr]);
        sqlExec(spu.sqlStmt(), spu.sqlParm());
        if (sqlRowNum <= 0) {
          wp.colSet(rr, "ok_flag", "X");
          wp.colSet(rr, "ls_errmsg", "up mkt_purc_gp err");
          llErr++;
          sqlCommit(0);
          continue;
        }

      } else {
        // insert
        busi.SqlPrepare spi = new SqlPrepare();
        spi.sql2Insert("mkt_purc_gp");
        spi.ppstr("group_code", aaGroupCode[rr]);
        spi.ppstr("description", aaDescription[rr]);
        spi.ppstr("reward_type", aaRewardType[rr]);
        spi.ppnum("base_amt_1", toNum(aaBaseAmt1[rr]));
        spi.ppnum("purch_amt_s_a1", toNum(aaPurchAmtSA1[rr]));
        spi.ppnum("purch_amt_e_a1", toNum(aaPurchAmtEA1[rr]));
        spi.ppnum("rate_a1", toNum(aaRateA1[rr]));
        spi.ppnum("purch_amt_s_a2", toNum(aaPurchAmtSA2[rr]));
        spi.ppnum("purch_amt_e_a2", toNum(aaPurchAmtEA2[rr]));
        spi.ppnum("rate_a2", toNum(aaRateA2[rr]));
        spi.ppnum("purch_amt_s_a3", toNum(aaPurchAmtSA3[rr]));
        spi.ppnum("purch_amt_e_a3", toNum(aaPurchAmtEA3[rr]));
        spi.ppnum("rate_a3", toNum(aaRateA3[rr]));
        spi.ppnum("purch_amt_s_a4", toNum(aaPurchAmtSA4[rr]));
        spi.ppnum("purch_amt_e_a4", toNum(aaPurchAmtEA4[rr]));
        spi.ppnum("rate_a4", toNum(aaRateA4[rr]));
        spi.ppnum("purch_amt_s_a5", toNum(aaPurchAmtSA5[rr]));
        spi.ppnum("purch_amt_e_a5", toNum(aaPurchAmtEA5[rr]));
        spi.ppnum("rate_a5", toNum(aaRateA5[rr]));
        spi.ppstr("mcht_no_1", aaMchtNo1[rr]);
        spi.ppstr("mcht_no_2", aaMchtNo2[rr]);
        spi.ppstr("mcht_no_3", aaMchtNo3[rr]);
        spi.ppstr("mcht_no_4", aaNchtno4[rr]);
        spi.ppstr("mcht_no_5", aaMchtNo5[rr]);
        spi.ppstr("mcht_no_6", aaMchtNo6[rr]);
        spi.ppstr("mcht_no_7", aaMchtNo7[rr]);
        spi.ppstr("mcht_no_8", aaMchtNo8[rr]);
        spi.ppstr("mcht_no_9", aaMchtNo9[rr]);
        spi.ppstr("mcht_no_10", aaMchtNo10[rr]);
        spi.ppnum("int_amt_s_1", toNum(aaIntAmtS1[rr]));
        spi.ppnum("int_amt_e_1", toNum(aaIntAmtE1[rr]));
        spi.ppnum("int_rate_1", toNum(aaIntRate1[rr]));
        spi.ppnum("int_amt_s_2", toNum(aaIntAmtS2[rr]));
        spi.ppnum("int_amt_e_2", toNum(aaIntAmtE2[rr]));
        spi.ppnum("int_rate_2", toNum(aaIntRate2[rr]));
        spi.ppnum("int_amt_s_3", toNum(aaIntAmtS3[rr]));
        spi.ppnum("int_amt_e_3", toNum(aaIntAmtE3[rr]));
        spi.ppnum("int_rate_3", toNum(aaIntRate3[rr]));
        spi.ppnum("int_amt_s_4", toNum(aaIntAmtS4[rr]));
        spi.ppnum("int_amt_e_4", toNum(aaIntAmtE4[rr]));
        spi.ppnum("int_rate_4", toNum(aaIntRate4[rr]));
        spi.ppnum("int_amt_s_5", toNum(aaIntAmtS5[rr]));
        spi.ppnum("int_amt_e_5", toNum(aaIntAmtE5[rr]));
        spi.ppnum("int_rate_5", toNum(aaIntRate5[rr]));
        spi.ppnum("out_amt_s_1", toNum(aaOutAmtS1[rr]));
        spi.ppnum("out_amt_e_1", toNum(aaOutAmtE1[rr]));
        spi.ppnum("out_rate_1", toNum(aaOutRate1[rr]));
        spi.ppnum("out_amt_s_2", toNum(aaOutAmtS2[rr]));
        spi.ppnum("out_amt_e_2", toNum(aaOutAmtE2[rr]));
        spi.ppnum("out_rate_2", toNum(aaOutRate2[rr]));
        spi.ppnum("out_amt_s_3", toNum(aaOutAmtS3[rr]));
        spi.ppnum("out_amt_e_3", toNum(aaOutAmtE3[rr]));
        spi.ppnum("out_rate_3", toNum(aaOutRate3[rr]));
        spi.ppnum("out_amt_s_4", toNum(aaOutAmtS4[rr]));
        spi.ppnum("out_amt_e_4", toNum(aaOutAmtE4[rr]));
        spi.ppnum("out_rate_4", toNum(aaOutRate4[rr]));
        spi.ppnum("out_amt_s_5", toNum(aaOutAmtS5[rr]));
        spi.ppnum("out_amt_e_5", toNum(aaOutAmtE5[rr]));
        spi.ppnum("out_rate_5", toNum(aaOutRate5[rr]));
        spi.ppstr("item_ename_bl", aaItemEnameBl[rr]);
        spi.ppstr("item_ename_bl_in", aaItemEnameBlIn[rr]);
        spi.ppstr("item_ename_bl_out", aaItemEnameBlOut[rr]);
        spi.ppstr("item_ename_it", aaItemEnameIt[rr]);
        spi.ppstr("item_ename_it_in", aaItemEnameItIn[rr]);
        spi.ppstr("item_ename_it_out", aaItemEnameItOut[rr]);
        spi.ppstr("item_ename_ca", aaItemEnameCa[rr]);
        spi.ppstr("item_ename_id", aaItemEnameId[rr]);
        spi.ppstr("item_ename_ao", aaItemEnameAo[rr]);
        spi.ppstr("item_ename_ot", aaItemEnameOt[rr]);
        spi.ppstr("present_type", aaPresentType[rr]);
        spi.ppnum("rate_a12", toNum(aaRateA12[rr]));
        spi.ppnum("rate_a22", toNum(aaRateA22[rr]));
        spi.ppnum("rate_a32", toNum(aaRateA32[rr]));
        spi.ppnum("rate_a42", toNum(aaRateA42[rr]));
        spi.ppnum("rate_a52", toNum(aaRateA52[rr]));
        spi.ppnum("int_rate_12", toNum(aaIntRate12[rr]));
        spi.ppnum("int_rate_22", toNum(aaIntRate22[rr]));
        spi.ppnum("int_rate_32", toNum(aaIntRate32[rr]));
        spi.ppnum("int_rate_42", toNum(aaIntRate42[rr]));
        spi.ppnum("int_rate_52", toNum(aaIntRate52[rr]));
        spi.ppnum("out_rate_12", toNum(aaOutRate12[rr]));
        spi.ppnum("out_rate_22", toNum(aaOutRate22[rr]));
        spi.ppnum("out_rate_32", toNum(aaOutRate32[rr]));
        spi.ppnum("out_rate_42", toNum(aaOutRate42[rr]));
        spi.ppnum("out_rate_52", toNum(aaOutRate52[rr]));
        spi.ppstr("program_code", aaProgramCode[rr]);
        spi.ppstr("purch_date_type", aaPurchDateType[rr]);
        spi.ppnum("run_time_dd", toInt(aaRunTimeDd[rr]));
        spi.ppstr("apr_flag", "Y");
        spi.ppstr("apr_date", getSysDate());
        spi.ppstr("apr_user", wp.loginUser);
        spi.ppstr("crt_user", wp.loginUser);
        spi.ppstr("crt_date", getSysDate());
        spi.ppstr("mod_pgm", wp.modPgm());
        spi.ppnum("mod_seqno", 1);
        sqlExec(spi.sqlStmt(), spi.sqlParm());
        if (sqlRowNum <= 0) {
          wp.colSet(rr, "ok_flag", "X");
          wp.colSet(rr, "ls_errmsg", "insert mkt_purc_gp err");
          llErr++;
          sqlCommit(0);
          continue;

        }
      }

      String lsProgCode = "", lsType = "", lsCode = "";
      // -Update ptr_bn_data-
      lsProgCode = aaProgramCode[rr];
      if (!empty(lsProgCode)) {
        String lsSqlptr =
            "select data_type,data_code from ptr_bn_data_t where program_code =:program_code ";
        setString("program_code", lsProgCode);
        sqlSelect(lsSqlptr);
        int selCt = sqlRowNum;
        if (selCt > 0) {
          // delete ptr_bn_data
          String lsDel = "delete   ptr_bn_data   where program_code  = :program_code  ";
          setString("program_code", lsProgCode);
          sqlExec(lsDel);

          for (int jj = 0; jj < selCt; jj++) {
            lsType = sqlStr(jj, "data_type");
            lsCode = sqlStr(jj, "data_code");
            String ls_insertdtl = " insert into ptr_bn_data ( " + "program_code," + "data_type,"
                + "data_code," + "mod_user," + "mod_time," + "mod_pgm," + "mod_seqno "
                + " ) values (?,?,?,?,sysdate,?,1)";
            Object[] param = new Object[] {lsProgCode, lsType, lsCode, wp.loginUser, wp.modPgm()};
            sqlExec(ls_insertdtl, param);
            if (sqlRowNum <= 0) {
              wp.colSet(rr, "ok_flag", "X");
              wp.colSet(rr, "ls_errmsg", "insert ptr_bn_data err");
              llErr++;
              sqlCommit(0);
              continue;
            }
          }
        }
        // delet ptr_bn_data_t
//        String ls_del2 = "delete  ptr_bn_data_t   where  program_code  = :program_code  ";
//        setString("program_code", lsProgCode);
//        // System.out.println("program_code : "+ls_prog_code);
//        sqlExec(ls_del2);
      }

      // mkt_purcgp_ext_t 20190625 add
      if (aaRewardType[rr].equals("2")) {
        String lsGroupCode = aaGroupCode[rr];
        lsSql = "select group_code, mcht_group_id " + "from mkt_purcgp_ext_t " + "where 1=1 "
            + "and group_code =:group_code";
        setString("group_code", lsGroupCode);
        sqlSelect(lsSql);
        int selCt1 = sqlRowNum;
        if (selCt1 > 0) {
          dsSql = "delete mkt_purcgp_ext where group_code =:group_code";
          setString("group_code", lsGroupCode);
          sqlExec(dsSql);
          for (int jj = 0; jj < selCt1; jj++) {
            String ls_mcht_group_id = sqlStr(jj, "mcht_group_id");
            isSql = "insert into mkt_purcgp_ext "
                + "(group_code, mcht_group_id, mod_user, mod_time, mod_pgm) " + "values ("
                + " :group_code, :mcht_group_id, :mod_user, sysdate, 'mktp0210')";
            setString("group_code", lsGroupCode);
            setString("mcht_group_id", ls_mcht_group_id);
            setString("mod_user", wp.loginUser);
            sqlExec(isSql);
            if (sqlRowNum <= 0) {
              wp.colSet(rr, "ok_flag", "X");
              wp.colSet(rr, "ls_errmsg", "insert mkt_purcgp_ext err");
              llErr++;
              sqlCommit(0);
              continue;
            }
          }
        }
        // delet mkt_purcgp_ext_t
        String lsDel2 = "delete  mkt_purcgp_ext_t  where  group_code  = :group_code  ";
        setString("group_code", lsGroupCode);
        sqlExec(lsDel2);
      }

      // delet mkt_purc_gp_t
      String lsDel3 = "delete  mkt_purc_gp_t   where  group_code  = :group_code  ";
      setString("group_code", aaGroupCode[rr]);
      sqlExec(lsDel3);

      wp.colSet(rr, "ok_flag", "V");
      llOk++;
      sqlCommit(1);
    }

      alertErr("執行處理: 成功筆數=" + llOk + "; 失敗筆數=" + llErr);

  }
  }
  public void commGroupCode(String columnData1) throws Exception {
	    String columnData = "";
	    String sql1 = "";
	    for (int ii = 0; ii < wp.selectCnt; ii++) {
	      columnData = "";
	      sql1 = "select " + " group_name as column_group_name " + " from ptr_group_code "
	          + " where 1 = 1 " + " and   group_code = '" + wp.colStr(ii, "group_code") + "'";
	      if (wp.colStr(ii, "group_code").length() == 0) {
	        continue;
	      }
	      sqlSelect(sql1);

	      if (sqlRowNum > 0) {
	        columnData = columnData + sqlStr("column_group_name");
	        wp.colSet(ii, columnData1, columnData);
	      }
	    }
	    return;
	  }

  
  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

//  @Override
//  public void dddwSelect() {
//    try {
//
//      wp.initOption = "--";
//      wp.optionKey = wp.itemStr("ex_user");
//      this.dddwList("dddw_sec_user", "sec_user", "usr_id", "usr_cname",
//          "where 1=1 order by usr_id");
//    } catch (Exception ex) {
//    }
//  }

}
