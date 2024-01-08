/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-01-09  V1.00.00  ryan       program initial                            *
* 109-01-06  V1.00.02  Justin Wu    updated for archit.  change                                                                           *
* 109-03-26  V1.00.03  Shiyuqi    修改参数                                                                                                       *  
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *                
******************************************************************************/

package mktm02;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Mktm4210 extends BaseProc {
  String asPinBlockFlag = "", msg = "", isAtm = "", errMsg = "";
  int llOk = 0, llErr = 0, ll = 0, i = 0;
  Mktm4210Func func;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

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
        /* 存檔 */
        strAction = "S2";
        dataProcess();
        break;
      case "S3":
        /* 刪除 */
        strAction = "S3";
        dataProcess();
        break;
      case "AJAX":
        // AJAX 20200106 updated for archit. change
        wfDw1Insert();
        break;
      default:
        break;
    }


    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("sysDate", wp.sysDate);
    wp.colSet("loginUser", wp.loginUser);
    wp.colSet("ex_cost_amt", "0");
    wp.colSet("tab_active", "tab1");
    wp.colSet("tab_active1", "id='tab_active'");
    wp.colSet("queryread", "tab1");
  }

  // for query use only
  public void getWhereStr() throws Exception {


    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_yymm1")) == false) {
      wp.whereStr += " and  cost_month >= :ex_yymm1 ";
      setString("ex_yymm1", wp.itemStr("ex_yymm1"));
    }

    if (empty(wp.itemStr("ex_yymm2")) == false) {
      wp.whereStr += " and  cost_month2 <= :ex_yymm2 ";
      setString("ex_yymm2", wp.itemStr("ex_yymm2"));
    }


    if (empty(wp.itemStr("ex_item_no")) == false) {
      wp.whereStr += " and  item_no = :ex_item_no ";
      setString("ex_item_no", wp.itemStr("ex_item_no"));
    }
    if (wp.itemStr("ex_item_no").equals("06")) {
      if (empty(wp.itemStr("ex_group_code")) == false) {
        wp.whereStr += " and  key_data = :ex_group_code ";
        setString("ex_group_code", wp.itemStr("ex_group_code"));
      }
    }
    if (wp.itemStr("ex_item_no").equals("08")) {
      if (empty(wp.itemStr("ex_mcht_no")) == false) {
        wp.whereStr += " and  trim(substrb(key_data,1,15)) = :ex_mcht_no ";
        setString("ex_mcht_no", wp.itemStr("ex_mcht_no"));
      }
      if (empty(wp.itemStr("ex_prod_no")) == false) {
        wp.whereStr += " and  trim(substrb(key_data,16,8)) = :ex_prod_no ";
        setString("ex_prod_no", wp.itemStr("ex_prod_no"));
      }
    }
    if (wp.itemStr("ex_item_no").equals("09") || wp.itemStr("ex_item_no").equals("10")
        || wp.itemStr("ex_item_no").equals("11") || wp.itemStr("ex_item_no").equals("12")
        || wp.itemStr("ex_item_no").equals("13")) {
      if (empty(wp.itemStr("ex_mcht_no")) == false) {
        wp.whereStr += " and  key_data = :ex_mcht_no ";
        setString("ex_mcht_no", wp.itemStr("ex_mcht_no"));
      }
    }

  }



  @Override
  public void queryFunc() throws Exception {
    String lsDate1 = wp.itemStr("ex_yymm1");
    String lsDate2 = wp.itemStr("ex_yymm2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[成本期間-起迄]  輸入錯誤");
      return;
    }
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    String tabActive = "";
    if (strAction.equals("Q")) {
      tabActive = wp.itemStr("tab_active");
    }
    if (strAction.equals("M")) {
      tabActive = wp.itemStr("queryread");
    }
    if (tabActive.equals("tab1")) {
      queryReadA();
      wp.colSet("tab_active1", "id='tab_active'");
      wp.colSet("tab_active2", "");
      wp.colSet("queryread", "tab1");
    }
    if (tabActive.equals("tab2")) {
      queryReadB();
      wp.colSet("tab_active2", "id='tab_active'");
      wp.colSet("tab_active1", "");
      wp.colSet("queryread", "tab2");
    }

    /*
     * if(msg.equals("a")){ alert_err("成本參數-待覆核,查無資料"); }else if(msg.equals("b")){
     * alert_err("成本參數-每月成本,查無資料"); }else if(msg.equals("ab")){ alert_err("查無資料"); }
     */

  }

  void queryReadA() throws Exception {
    daoTid = "A-";
    wp.pageControl();
    wp.selectSQL = " hex(rowid) as rowid " + " ,item_no " + " ,input_type" + " ,key_data "
        + " ,key_type " + " ,cost_month " + " ,cost_amt " + " ,cost_month2 "
        + " ,exist_cost_months " + " ,crt_user " + " ,crt_date " + " ,mod_seqno "
        + " ,decode(key_type,'1',key_data,'') db_group_code "
        + " ,decode(key_type,'3',key_data,'4',trim(substrb(key_data,1,15)),'') db_mcht_no "
        + " ,decode(key_type,'4',trim(substrb(key_data,16,8)),'') db_prod_no " + " ,purch_mm "
        + " ,service_amt " + " ,mod_time " + " ,aud_code " + " ,item_ename_incl "
        + " ,item_ename_excl " + " ,bonu_cash_flag ";
    getWhereStr();
    wp.daoTable = " mkt_contri_parm_t ";
    wp.whereOrder = " ";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdataA();
    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    daoTid = "a-";
  }

  void queryReadB() throws Exception {
    daoTid = "B-";
    wp.pageControl();
    wp.selectSQL = " hex(rowid) as rowid " + " ,item_no " + " ,key_data " + " ,key_type "
        + " ,cost_month " + " ,cost_amt " + " ,crt_user " + " ,crt_date " + " ,apr_date "
        + " ,apr_user " + " ,mod_seqno " + " ,decode(key_type,'1',key_data,'') db_group_code "
        + " ,decode(key_type,'3',key_data,'4',trim(substrb(key_data,1,15)),'') db_mcht_no "
        + " ,decode(key_type,'4',trim(substrb(key_data,16,8)),'') db_prod_no " + " ,purch_mm "
        + " ,service_amt ";
    getWhereStr();
    wp.daoTable = " mkt_contri_parm ";
    wp.whereOrder = " ";
    pageQuery();
    wp.setListCount(2);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdataB();
    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    daoTid = "b-";
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
    if (strAction.equals("S2")) {
      dataProcessA();
    }
    if (strAction.equals("S3")) {
      dataProcessB();
    }
  }

  @Override
  public void initButton() {
    this.btnModeAud("XX");

  }

  @Override
  public void dddwSelect() {
    daoTid = "";
    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_group_code");

      dddwList("dddw_group_code", "Ptr_group_code", "group_code", "group_name",
          "where 1=1  order by group_code");
    } catch (Exception ex) {
    }
  }

  public void dataProcessA() throws Exception {
    String[] aOpt = wp.itemBuff("a_opt");
    String[] aRowid = wp.itemBuff("a_rowid");
    String[] aModSeqno = wp.itemBuff("a_mod_seqno");
    String[] aItemNo = wp.itemBuff("a_item_no");
    String[] aInputType = wp.itemBuff("a_input_type");
    String[] aModTime = wp.itemBuff("a_mod_time");
    String[] aCostMonth = wp.itemBuff("a_cost_month");
    String[] aCostMonth2 = wp.itemBuff("a_cost_month2");
    String[] aCostAmt = wp.itemBuff("a_cost_amt");
    String[] aPurchMm = wp.itemBuff("a_purch_mm");
    String[] aServiceAmt = wp.itemBuff("a_service_amt");
    String[] aExistCostMonths = wp.itemBuff("a_exist_cost_months");
    String[] aCrtUser = wp.itemBuff("a_crt_user");
    String[] aCrtDate = wp.itemBuff("a_crt_date");
    String[] aDbGroupCode = wp.itemBuff("a_db_group_code");
    String[] aDbMchtNo = wp.itemBuff("a_db_mcht_no");
    String[] aDbProdNo = wp.itemBuff("a_db_prod_no");
    String lsKey = "", lsKeyType = "", lsDbMchtNo = "", lsDbProdNo = "";

    wp.listCount[0] = aRowid.length;
    func = new Mktm4210Func(wp);
    for (ll = 0; ll < aItemNo.length; ll++) {
      if (aItemNo[ll].equals("06")) {
        lsKey = aDbGroupCode[ll];
        lsKeyType = "1";
      }
      if (aItemNo[ll].equals("07")) {
        lsKey = "x";
        lsKeyType = "0";
      }
      if (aItemNo[ll].equals("08")) {
        lsDbMchtNo = aDbMchtNo[ll];
        for (int i = lsDbMchtNo.length(); i < 15; i++) {
          lsDbMchtNo += " ";
        }
        lsDbProdNo = aDbProdNo[ll];
        for (int i = lsDbProdNo.length(); i < 8; i++) {
          lsDbProdNo += " ";
        }
        lsKey = lsDbMchtNo + lsDbProdNo;
        lsKeyType = "4";
      }
      if (aItemNo[ll].equals("09") || aItemNo[ll].equals("10") || aItemNo[ll].equals("11")
          || aItemNo[ll].equals("12") || aItemNo[ll].equals("13")) {
        lsKey = aDbMchtNo[ll];
        lsKeyType = "3";
      }
      func.varsSet("a_rowid", aRowid[ll]);
      func.varsSet("a_mod_seqno", aModSeqno[ll]);
      func.varsSet("a_item_no", aItemNo[ll]);
      func.varsSet("a_input_type", aInputType[ll]);
      func.varsSet("a_key_data", lsKey);
      func.varsSet("a_key_type", lsKeyType);
      func.varsSet("a_mod_time", aModTime[ll]);
      func.varsSet("a_cost_month", aCostMonth[ll]);
      func.varsSet("a_cost_month2", aCostMonth2[ll]);
      func.varsSet("a_cost_amt", aCostAmt[ll]);
      func.varsSet("a_purch_mm", aPurchMm[ll]);
      func.varsSet("a_service_amt", aServiceAmt[ll]);
      func.varsSet("a_exist_cost_months", aExistCostMonths[ll]);
      func.varsSet("a_crt_user", aCrtUser[ll]);
      func.varsSet("a_crt_date", aCrtDate[ll]);
      // -option-ON-delete
      // -delete no-approve-

      if (checkBoxOptOn(ll, aOpt)) {
        if (empty(aRowid[ll]) == false) {
          if (func.dbDelete() != 1) {

            wp.colSet("a-ok_flag", "!");
            sqlCommit(0);
            alertErr("delete mkt_contri_parm_t err");
            return;
          }
          continue;
        }
      } else {
        if (empty(aRowid[ll]) == false) {
          continue;
        } else {
          String sqlSelect = " select * " + " from mkt_contri_parm_t "
              + " where item_no = :item_no " + " and key_data = :key_data "
              + " and key_type = :key_type " + " and cost_month = :cost_month ";
          setString("item_no", aItemNo[ll]);
          setString("key_data", lsKey);
          setString("key_type", lsKeyType);
          setString("cost_month", aCostMonth[ll]);

          sqlSelect(sqlSelect);

          if (sqlRowNum > 0) {

            // update
            if (func.dbUpdate() != 1) {

              wp.colSet("a-ok_flag", "!");
              sqlCommit(0);
              alertErr("update mkt_contri_parm_t err");
              return;
            }
          } else {

            // insert
            if (func.dbInsert() != 1) {

              wp.colSet("a-ok_flag", "!");
              sqlCommit(0);
              alertErr("insert mkt_contri_parm_t err");
              return;
            }
          }
        }
      }
    }
    sqlCommit(1);
    strAction = "Q";
    queryFunc();
    errmsg("待覆核資料存檔成功");
  }

  public void dataProcessB() throws Exception {
    String[] bOpt = wp.itemBuff("b-opt");
    String[] bRowid = wp.itemBuff("b-rowid");
    String[] bModSeqno = wp.itemBuff("b-mod_seqno");
    wp.listCount[1] = bRowid.length;

    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }
    func = new Mktm4210Func(wp);

    for (ll = 0; ll < bRowid.length; ll++) {
      func.varsSet("b_mod_seqno", bModSeqno[ll]);
      func.varsSet("b_rowid", bRowid[ll]);
      // -delete
      if (checkBoxOptOn(ll, bOpt)) {
        if (func.dbDelete2() != 1) {
          // wp.col_set("b-ok_flag", "!");
          llErr++;
          sqlCommit(0);
          continue;
        }
        sqlCommit(1);
        llOk++;
      }
    }
    strAction = "Q";
    queryFunc();
    errmsg("成本參數刪除處理: 成功筆數= " + llOk + "; 失敗筆數= " + llErr);

  }

  public void listWkdataA() {
    String tmpStr = "";
    daoTid = "a-";
    for (int ll = 0; ll < wp.selectCnt; ll++) {

      tmpStr = wp.colStr(ll, daoTid + "item_no");
      wp.colSet(ll, daoTid + "tt_item_no", commString.decode(tmpStr, ",06,07,08,09,10,11,12,13",
          ",06.肖像授權費,07.帳單費用,08.機場接送,09.機場停車,10.龍騰卡貴賓室,11.市區停車,12.影城優惠,13.新貴通貴賓室"));
    }
  }

  public void listWkdataB() {
    String tmpStr = "";
    daoTid = "b-";
    for (int ll = 0; ll < wp.selectCnt; ll++) {

      tmpStr = wp.colStr(ll, daoTid + "item_no");
      wp.colSet(ll, daoTid + "tt_item_no", commString.decode(tmpStr, ",06,07,08,09,10,11,12,13",
          ",06.肖像授權費,07.帳單費用,08.機場接送,09.機場停車,10.龍騰卡貴賓室,11.市區停車,12.影城優惠,13.新貴通貴賓室"));
    }
  }

  public void wfDw1Insert() throws Exception {
    // super.wp = wr; // 20200102 updated for archit. change
    String textData1 = wp.itemStr("text_data1");
    String textData2 = wp.itemStr("text_data2");
    String textData3 = wp.itemStr("text_data3");
    if (wp.itemStr("data_kk").equals("1")) {
      String sqlSelect = "select * from ptr_group_code where group_code =:is_key_data ";
      setString("is_key_data", wp.itemStr("ex_group_code_kk"));
      sqlSelect(sqlSelect);
      if (sqlRowNum <= 0) {
        wp.colSet("data_check", "");
        wp.addJSON("data_check", "");
        wp.colSet("data_msg", "團體代號 不存在");
        wp.addJSON("data_msg", "團體代號 不存在");
        return;
      }
    }

    if (wp.itemStr("data_kk").equals("3")) {
      if (wp.itemStr("ex_item_no").equals("11")) {
        String sqlSelect = "select * from ofw_sysparm " + " where wf_parm='SYSPARM' "
            + " and  wf_key like 'MKT_DODO_RESP_%' " + " and  wf_value =:is_key_data ";
        setString("is_key_data", wp.itemStr("ex_mcht_no_kk"));
        sqlSelect(sqlSelect);
      } else {
        String sqlSelect = "select * from bil_merchant " + " where  mcht_no =:is_key_data ";
        setString("is_key_data", wp.itemStr("ex_mcht_no_kk"));
        sqlSelect(sqlSelect);
      }
      if (sqlRowNum <= 0) {
        wp.colSet("data_check", "");
        wp.addJSON("data_check", "");
        wp.colSet("data_msg", "特店代號 不存在");
        wp.addJSON("data_msg", "特店代號 不存在");
        return;
      }
    }
    if (wp.itemStr("data_kk").equals("4")) {
      String sqlSelect = "select * from bil_prod " + " where mcht_no = :is_key_data1 "
          + " and product_no =:is_key_data2 ";
      setString("is_key_data1", wp.itemStr("ex_mcht_no_kk"));
      setString("is_key_data2", wp.itemStr("ex_prod_no_kk"));
      sqlSelect(sqlSelect);
      if (sqlRowNum <= 0) {
        wp.colSet("data_check", "");
        wp.addJSON("data_check", "");
        wp.colSet("data_msg", "商品~代號 不存在");
        wp.addJSON("data_msg", "商品~代號 不存在");
        return;
      }
    }
    String sqlSelect1 = " select * " + " from mkt_contri_parm " + " where item_no =:is_item_no "
        + " and key_type =:ls_key_type " + " and cost_month between :is_yymm1 and :is_yymm2 "
        + " and   rpad(key_data,23,' ') = rpad(:ls_data1,15,' ')||rpad(:ls_data2,8,' ') ";
    setString("is_item_no", wp.itemStr("ex_item_no_kk"));
    setString("ls_key_type", wp.itemStr("data_kk"));
    setString("is_yymm1", wp.itemStr("ex_yymm1_kk"));
    setString("is_yymm2", wp.itemStr("ex_yymm2_kk"));
    setString("ls_data1", wp.itemStr("data_kk2"));
    setString("ls_data2", wp.itemStr("data_kk3"));
    sqlSelect(sqlSelect1);
    wp.colSet("exist_cost_months", sqlRowNum + "");
    wp.addJSON("exist_cost_months", sqlRowNum + "");

    String sqlSelect = "select * " + " from mkt_contri_parm_t " + " where item_no = :is_item_no "
        + " and   input_type ='2' " + " and   key_type = :ls_key_type "
        + " and   rpad(key_data,23,' ') = rpad( :ls_data1,15,' ')||rpad( :ls_data2,8,' ') ";
    setString("is_item_no", wp.itemStr("ex_item_no_kk"));
    setString("ls_key_type", wp.itemStr("data_kk"));
    setString("ls_data1", wp.itemStr("data_kk2"));
    setString("ls_data2", wp.itemStr("data_kk3"));
    sqlSelect(sqlSelect);
    if (sqlRowNum > 0) {
      wp.colSet("data_check", "Y");
      wp.addJSON("data_check", "Y");
    } else {
      wp.colSet("data_check", "N");
      wp.addJSON("data_check", "N");
    }
    wp.colSet("text_data1", textData1);
    wp.colSet("text_data2", textData2);
    wp.colSet("text_data3", textData3);
    wp.addJSON("text_data1", textData1);
    wp.addJSON("text_data2", textData2);
    wp.addJSON("text_data3", textData3);
  }
}
