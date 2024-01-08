/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-13  V1.00.00  Andy       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 107-05-15  V1.00.02  Andy		  update : UI,Debug                          *
* 107-08-28  V1.00.03  Andy		  update : UI,新增需求                       *                                          * 
* 108-12-03  V1.00.04  Amber	  Update init_button Authority 	     		 *	
* 109-01-02  V1.00.05  JustinWu   updated for archit.  change                *
* 109-04-20  V1.00.06  shiyuqi    updated for project coding standard        *
* 110-03-31  V1.00.07  JustinWu   fix XSS 
* 111-04-14  V1.00.08  machao     TSC畫面整合                                   *
* ******************************************************************************/

package tscm01;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Tscm0020 extends BaseEdit {
  String mExCardType = "";
  String mExGroupCode = "";
  String mExTscBinNo = "";

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
        // -資料讀取-
        strAction = "R";
        dataRead();
        break;
      case "A":
        /* 新增功能 */
        insertFunc();
        break;
      case "U":
        /* 更新功能 */
        updateFunc();
        break;
      case "D":
        /* 刪除功能 */
        deleteFunc();
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
        saveFunc();
        break;
      case "AJAX":
        // AJAX 20200102 updated for archit. change
        itemChange();
        break;
      default:
        break;
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    String lsSql = "", addHtml = "";
    lsSql =
        "select tsc_bin_no as htsc_bin_no, LPAD(seq_no_current, 10, '0') as hseq_no_current "
            + "from tsc_bin_curr order by tsc_bin_no ";
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      for (int ii = 0; ii < sqlRowNum; ii++) {
    	  wp.colSet(ii, "tdNo", ii + 1);
    	  wp.colSet(ii, "tdTscBinNo", sqlStr(ii, "htsc_bin_no"));
    	  wp.colSet(ii, "tdSeqNoCurrent", sqlStr(ii, "hseq_no_current"));
    	  wp.colSet(ii, "btOther_disable", "disabled");
//        addHtml += "<tr>" + "<td nowrap class='list_cc'>" + (ii + 1) + "</td>" // NO
//            + "<td nowrap class='list_cc'>" + sqlStr(ii, "htsc_bin_no") + "</td>" // tsc_bin_no
//            + "<td nowrap class='list_cc'>" + sqlStr(ii, "hseq_no_current") + "</td>" // seq_no_current
//            + "</tr>";
      }
//      wp.colSet("add_html", addHtml);
		try {
			wp.selectCnt = sqlRowNum;
			wp.setListCount(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = "";
    String exCardType = wp.itemStr("ex_card_type");
    String exGroupCode = wp.itemStr("ex_group_code");
    String exTscBinNo = wp.itemStr("ex_tsc_bin_no");
    String exBinNo = wp.itemStr("ex_bin_no");
    wp.whereStr = " where 1=1 ";
    // 固定條件

    // 自鍵條件
    // if(empty(ex_card_type) && empty(ex_group_code) && empty(ex_bin_no)){
    // alert_err("請至少輸入一項查詢條件");
    // return false;
    // }
    if (empty(exCardType) == false) {
      wp.whereStr += sqlCol(exCardType, "card_type");
    }
    if (empty(exGroupCode) == false) {
      wp.whereStr += sqlCol(exGroupCode, "group_code");
    }
    if (exTscBinNo.equals("0") == false) {
      wp.whereStr += sqlCol(exTscBinNo, "tsc_bin_no");
    }
    if (empty(exBinNo) == false) {
      wp.whereStr += sqlCol(exBinNo, "tsc_bin_no");
    }
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
//    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
//    if (getWhereStr() == false)
//      return;
    wp.pageControl();

    wp.selectSQL = "hex (rowid) AS rowid, card_type, group_code, tsc_bin_no, "
        + "mod_user, mod_time, mod_pgm, mod_seqno ";
    wp.daoTable = "tsc_bintable";
    wp.whereOrder = " order by tsc_bin_no,group_code,card_type ";
    getWhereStr();
    // System.out.println("select " + wp.selectSQL + " from " + wp.daoTable
    // + wp.whereStr + wp.whereOrder);
    pageQuery();

    wp.setListCount(2);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      listWkdata();
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
    String lsSql = "", addHtml = "";

    lsSql =
        "select tsc_bin_no as htsc_bin_no, LPAD(seq_no_current, 10, '0') as hseq_no_current "
            + "from tsc_bin_curr order by tsc_bin_no ";
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      for (int ii = 0; ii < sqlRowNum; ii++) {
    	  wp.colSet(ii, "tdNo", ii + 1);
    	  wp.colSet(ii, "tdTscBinNo", sqlStr(ii, "htsc_bin_no"));
    	  wp.colSet(ii, "tdSeqNoCurrent", sqlStr(ii, "hseq_no_current"));
    	  wp.colSet(ii, "btOther_disable", "disabled");
//        addHtml += "<tr>" + "<td nowrap class='list_cc'>" + (ii + 1) + "</td>" // NO
//            + "<td nowrap class='list_cc'>" + sqlStr(ii, "htsc_bin_no") + "</td>" // tsc_bin_no
//            + "<td nowrap class='list_cc'>" + sqlStr(ii, "hseq_no_current") + "</td>" // seq_no_current
//            + "</tr>";
      }
//      wp.colSet("add_html", addHtml);
      wp.selectCnt = sqlRowNum;
      wp.setListCount(1);
    }
  }

  @Override
  public void querySelect() throws Exception {
    // m_ex_pp_card_no = wp.item_ss("pp_card_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {
    String[] aaRowid = wp.itemBuff("rowid");
    String[] aaModSeqno = wp.itemBuff("mod_seqno");
    String[] opt = wp.itemBuff("opt");

    String[] aaCardType = wp.itemBuff("card_type");
    String[] aaGroupCode = wp.itemBuff("group_code");
    String[] aaTscBinNo = wp.itemBuff("tsc_bin_no");
    String isSql = "", usSql = "";
    //
    wp.listCount[0] = aaCardType.length;

    String lsSql = "";
    int llOk = 0, llErr = 0, rr = 0;
    // check
    for (int ii = 0; ii < opt.length; ii++) {
      String mRowid = "", mModSeqno = "";

      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
        continue;
      }
      mRowid = nvl(aaRowid[rr]);
      wp.colSet(rr, "set_opt", "checked");
      // 勾選刪除資料處理
      // if (checkBox_opt_on(rr, opt)) {
      // if (empty(m_rowid)) {
      // continue;
      // } else {
      // wp.col_set(rr, "err_msg", "檢核成功!");
      // }
      // }
      // 未勾選資料處理
      if (!checkBoxOptOn(rr, opt)) {
        if (!empty(mRowid))
          continue;
        if (empty(aaCardType[rr])) {
          wp.colSet(rr, "err_msg", "卡種資料未輸入!");
          wp.colSet(rr, "ok_flag", "X");
          llErr++;
          continue;
        }
        if (empty(aaGroupCode[rr])) {
          wp.colSet(rr, "err_msg", "團體代號未輸入!");
          wp.colSet(rr, "ok_flag", "X");
          llErr++;
          continue;
        }
        if (empty(aaTscBinNo[rr])) {
          wp.colSet(rr, "err_msg", "bin_no未輸入!");
          wp.colSet(rr, "ok_flag", "X");
          llErr++;
          continue;
        }
        lsSql = "select count(*) ct  from tsc_bintable  where card_type =:card_type "
            + "and group_code =:group_code  and tsc_bin_no =:tsc_bin_no ";
        setString("card_type", aaCardType[rr]);
        setString("group_code", aaGroupCode[rr]);
        setString("tsc_bin_no", aaTscBinNo[rr]);
        sqlSelect(lsSql);
        if (sqlInt("ct") > 0) {
          wp.colSet(rr, "err_msg", "資料值重複!!");
          llErr++;
          continue;
        }
        // wp.col_set(rr, "err_msg", "檢核成功!");
      }
    }
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      llErr = 999;
    }
    if (llErr > 0) {
      // alert_err("資料有誤,請重新檢核!!");
      return;
    }
    // save
    if (llErr == 0) {
      for (int ii = 0; ii < aaCardType.length; ii++) {
        String mRowid = "", mModSeqno = "";
        mRowid = nvl(aaRowid[ii]);
        // 勾選刪除資料處理
        if (checkBoxOptOn(ii, opt)) {
          if (empty(mRowid)) {
            continue;
          } else {
            if (deleteTscBintable(mRowid, mModSeqno, ii) != 1) {
              wp.colSet(ii, "ok_flag", "X");
              wp.colSet(ii, "err_msg", "資料刪除失敗");
              llErr++;
              continue;
            } else {
              llOk++;
              // wp.col_set(ii, "err_msg", "檢核成功!");
            }
          }
        }
        // 未勾選資料處理
        if (!checkBoxOptOn(ii, opt)) {
          if (!empty(mRowid))
            continue;
          // tsc_bintable
          if (empty(aaRowid[ii])) {
            isSql = "insert into tsc_bintable (card_type, group_code, tsc_bin_no, "
                + "mod_user, mod_time, mod_pgm, mod_seqno)  values ("
                + ":card_type, :group_code, :tsc_bin_no, :mod_user, sysdate, :mod_pgm, 1)";
            setString("card_type", aaCardType[ii]);
            setString("group_code", aaGroupCode[ii]);
            setString("tsc_bin_no", aaTscBinNo[ii]);
            setString("mod_user", wp.loginUser);
            setString("mod_pgm", wp.modPgm());
            // System.out.println("is_sql :" + is_sql);
            sqlExec(isSql);
            if (sqlRowNum <= 0) {
              wp.colSet(ii, "err_msg", "Insert tsc_bintable error");
              wp.colSet(ii, "ok_flag", "X");
              llErr++;
              continue;
            }
            // tsc_bin_curr
            if (wfTscBinCurr(ii) != 1) {
              llErr++;
              continue;
            } else {
              llOk++;
            }
          }
        }
      }
   
      // update tsc_bin_curr
      updateTscBinCurr();
    }

    if (llErr > 0) {
      sqlCommit(0);
      alertMsg("資料異動失敗");
      return;
    }
    sqlCommit(1);
    for (int ii = 0; ii < aaCardType.length; ii++) {
      if (checkBoxOptOn(ii, opt)) {
        wp.colSet(ii, "ok_flag", "V");
        wp.colSet(ii, "err_msg", "");
      }
      if (!checkBoxOptOn(ii, opt) && empty(aaRowid[ii])) {
        wp.colSet(ii, "ok_flag", "V");
        wp.colSet(ii, "err_msg", "");
      }
    }
    alertMsg("資料異動成功");
    listWkdata();
  }

  @Override
  public void initButton() {
    // if (wp.respHtml.indexOf("_detl") > 0) {
    // this.btnMode_aud();
    // }
    btnUpdateOn(wp.autUpdate());
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_group_code");
      // this.dddw_list("dddw_group_code", "ptr_group_code", "group_code", "group_name", " where 1=1
      // order by group_code");
      this.dddwList("dddw_group_code", "crd_item_unit", "unit_code", "",
          " where  1=1  group by unit_code order by unit_code");

      wp.optionKey = wp.itemStr("ex_card_type");
      this.dddwList("dddw_card_type", "ptr_card_type", "card_type", "name",
          " where  1=1  order by card_type");

      wp.optionKey = wp.itemStr("kk_group_code");
      // this.dddw_list("dddw_group_code_h", "ptr_group_code",
      // "group_code", "group_name", " where 1=1 order by group_code");
      this.dddwList("dddw_group_code_h", "crd_item_unit", "unit_code", "",
          " where  1=1  group by unit_code order by unit_code");

      wp.optionKey = wp.itemStr("kk_card_type");
      this.dddwList("dddw_card_type_h", "ptr_card_type", "card_type", "name",
          " where 1=1 order by card_type");
    } catch (Exception ex) {
    }
  }

  public int deleteTscBintable(String rowid, String modSeqno, int ii) throws Exception {
    String[] aaTscBinNo = wp.itemBuff("tsc_bin_no");
    String lsSql = "";
    String dsSql = "delete tsc_bintable where 1=1 ";
    dsSql += sqlCol(rowid, "hex(rowid)");
    dsSql += sqlCol(modSeqno, "mod_seqno");
    sqlExec(dsSql);
    if (sqlRowNum <= 0) {
      return -1;
    }
    // 如delete tsc_bintable後已無該tsc_bin_no資料,則一併刪除tsc_bin_curr
    lsSql = "select count(*) ct from tsc_bintable where tsc_bin_no =:tsc_bin_no ";
    setString("tsc_bin_no", aaTscBinNo[ii]);
    sqlSelect(lsSql);
    if (sqlNum("ct") == 0) {
      dsSql = "delete tsc_bin_curr where tsc_bin_no =:tsc_bin_no ";
      setString("tsc_bin_no", aaTscBinNo[ii]);
      sqlExec(dsSql);
      if (sqlRowNum <= 0) {
        return -1;
      }
    }
    return 1;
  }

  public int wfTscBinCurr(int ii) {
    String lsSql = "", isSql = "";
    String[] aaTscBinNo = wp.itemBuff("tsc_bin_no");
    lsSql = "select tsc_bin_no from tsc_bin_curr where tsc_bin_no =:tsc_bin_no ";
    setString("tsc_bin_no", aaTscBinNo[ii]);
    sqlSelect(lsSql);
    if (sqlRowNum == 0) {
      isSql = "insert into tsc_bin_curr (tsc_bin_no, seq_no_current, "
          + "mod_user, mod_time, mod_pgm, mod_seqno) values ( :tsc_bin_no, 0, "
          + ":mod_user, sysdate, :mod_pgm, 0)";
      setString("tsc_bin_no", aaTscBinNo[ii]);
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.modPgm());
      sqlExec(isSql);
      if (sqlRowNum < 0) {
        wp.colSet(ii, "err_msg", "Insert tsc_bin_curr error");
        wp.colSet(ii, "ok_flag", "X");
        return -1;
      } else {
        return 1;
      }
    }
    return 1;
  }
  
	void updateTscBinCurr() {
		String lsSql = "", isSql = "";
		String[] aaTscBinNo = wp.itemBuff("tdTscBinNo");
		String[] tdSeqNoCurrent = wp.itemBuff("tdSeqNoCurrent");
		for (int ii = 0; ii < aaTscBinNo.length; ii++) {
			lsSql = "select count(*) curr_cnt from tsc_bin_curr where tsc_bin_no =:tsc_bin_no ";
			setString("tsc_bin_no", aaTscBinNo[ii]);
			sqlSelect(lsSql);
			if (sqlNum("curr_cnt") > 0) {
				isSql = "update tsc_bin_curr set seq_no_current = :seq_no_current where tsc_bin_no = :tsc_bin_no ";
				setString("seq_no_current", tdSeqNoCurrent[ii]);
				setString("tsc_bin_no", aaTscBinNo[ii]);
				sqlExec(isSql);
			}
		}
	}

  public int itemChange() throws Exception {
    // super.wp = wr; //20200102 updated for archit. change
    String val = wp.itemStr("data_kk");;
    String dddWhere = "", option = "";
    setSelectLimit(0);
    if (!empty(val))
      dddWhere = " and card_type = :card_type ";
    String selectBilProd = "select unit_code from crd_item_unit where 1=1 " + dddWhere
        + " order by unit_code ";
    setString("card_type", val);
    sqlSelect(selectBilProd);
    if (sqlRowNum <= 0) {
      option += "<option value=\"\">--</option>";
    } else {
      option += "<option value=\"\">--</option>";
      for (int ii = 0; ii < sqlRowNum; ii++) {
        option += "<option value=\"" + sqlStr(ii, "unit_code") + "\" ${tot_term-"
            + sqlStr(ii, "unit_code") + "} >" + sqlStr(ii, "unit_code") + "</option>";
      }
    }
    wp.addJSON("dddw_group_code_h2", option);

    return 1;
  }
}
