/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 2019-1213  V1.00.01  Alex       add initButton
* 109-04-27  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
* 110-01-05  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package mktm05;

import java.util.Arrays;

import ofcapp.BaseAction;

public class Mktm5040 extends BaseAction {
  String validDate = "", paramType = "", dataKK3 = "";

  @Override
  public void userAction() throws Exception {
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
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    }

  }

  @Override
  public void dddwSelect() {

  }

  @Override
  public void queryFunc() throws Exception {
    /*
     * String ls_where = " where param_type ='3' "
     * +sql_col(wp.item_ss("ex_valid_date"),"valid_date",">=") ;
     * 
     * if(!wp.item_eq("ex_apr_flag", "0")){ ls_where +=
     * sql_col(wp.item_ss("ex_apr_flag"),"apr_flag"); }
     * 
     * wp.whereStr = ls_where; wp.queryWhere = wp.whereStr; wp.setQueryMode();
     * 
     * queryRead();
     */
  }

  @Override
  public void queryRead() throws Exception {
    /*
     * wp.pageControl();
     * 
     * wp.selectSQL = " valid_date , " + " sales_hire_day , " + " cut_card_mon , " +
     * " sales_amt_max , " + " season_point , " + " season_amt , " + " alive_cond_pct , " +
     * " alive_amt_pct , " + " mod_user , " + " to_char(mod_time,'yyyymmdd') as mod_date , " +
     * " apr_user , " + " apr_date , " + " apr_flag , " + " param_type " ;
     * 
     * wp.daoTable = " mkt_ds_parm1 ";
     * 
     * pageQuery();
     * 
     * if(sql_notFind()){ err_alert("此條件查無資料"); return ; }
     * 
     * wp.setListCount(0); wp.setPageValue();
     */
  }

  @Override
  public void querySelect() throws Exception {
    validDate = wp.itemStr("data_k1");
    paramType = wp.itemStr("data_k2");
    dataKK3 = wp.itemStr("data_k3");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(validDate))
      validDate = "00000000";
    if (empty(paramType))
      paramType = "3";
    if (empty(dataKK3))
      dataKK3 = "Y";
    wp.selectSQL = " valid_date , " + " apr_flag , " + " sales_hire_day , " + " cut_card_mon , "
        + " sales_amt_max , " + " season_point , " + " season_amt , " + " alive_cond_pct , "
        + " alive_amt_pct , " + " apr_date , " + " apr_user , " + " crt_user , " + " crt_date , "
        + " mod_user , " + " to_char(mod_time,'yyyymmdd') as mod_date , " + " mod_pgm , "
        + " mod_seqno , " + " hex(rowid) as rowid ";

    wp.daoTable = " mkt_ds_parm1 ";
    wp.whereStr = " where 1=1 " + sqlCol(validDate, "valid_date") + sqlCol(paramType, "param_type");

    pageSelect();

    if (sqlNotFind()) {
      this.selectOK();
      // err_alert("此條件查無資料");
      return;
    }

    selectDetail();

  }

  void selectDetail() throws Exception {
    wp.pageRows = 999;
    wp.selectSQL = " data_code1 , " + " data_remark1 , " + " point_amt , "
        + " data_code1||data_remark1||point_amt as old_data , "
        + " to_char(mod_time,'yyyymmdd') as mod_date ";

    wp.daoTable = " mkt_ds_parm1_detl ";
    wp.whereStr = " where 1=1 " + sqlCol(validDate, "valid_date") + sqlCol(paramType, "param_type")
        + sqlCol(wp.colStr("apr_flag"), "apr_flag");
    wp.whereOrder = " order by data_code1 ";
    pageQuery();

    if (sqlNotFind()) {
      selectOK();
      wp.colSet("ind_num", 0);
      return;
    }

    wp.setListCount(0);
    wp.colSet("ind_num", wp.selectCnt);
    calEndRemark();

  }

  void calEndRemark() {
    int ilSelectCnt = 0;
    ilSelectCnt = wp.selectCnt;
    double liEndReamrk = 0.0;
    int zz = 0;
    for (int ii = 0; ii < ilSelectCnt; ii++) {
      zz++;
      if (zz == ilSelectCnt)
        break;
      liEndReamrk = wp.colNum(zz, "data_remark1") - 0.01;
      liEndReamrk = commString.numScale(liEndReamrk, 2);
      wp.colSet(ii, "end_remark", liEndReamrk);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    keepList();

    if (checkApproveZz() == false)
      return;

    Mktm5040Func func = new Mktm5040Func();
    func.setConn(wp);

    if (!wp.itemEmpty("rowid")) {
      rc = func.dbUpdate();
    } else {
      rc = func.dbInsert();
    }

    if (rc != 1) {
      errmsg(func.getMsg());
      return;
    }

    if (isAdd() || isUpdate()) {
      procFunc();
    }

    saveAfter(false);
  }

  void keepList() {
    String[] aa_opt = wp.itemBuff("opt");
    wp.listCount[0] = wp.itemRows("data_code1");
    optNumKeep(wp.itemRows("data_code1"), aa_opt);
  }

  @Override
  public void procFunc() throws Exception {
    int llOk = 0, llErr = 0;

    String[] aaOpt = wp.itemBuff("opt");
    String[] lsDataCode1 = wp.itemBuff("data_code1");
    String[] lsDataRemark1 = wp.itemBuff("data_remark1");
    String[] lsPointAmt = wp.itemBuff("point_amt");
    String[] lsOldData = wp.itemBuff("old_data");
    String lsValidDate = "00000000";
    String lsParamType = wp.itemStr("param_type");
    String lsPromoteType = wp.itemStr("promote_type");

    Mktm5040Func func = new Mktm5040Func();
    func.setConn(wp);

    int ll = -1;
    for (String tmpStr : lsPointAmt) {
      ll++;
      wp.colSet(ll, "ok_flag", "");
      // -option-ON-
      if (checkBoxOptOn(ll, aaOpt)) {
        continue;
      }

      if (empty(tmpStr)) {
        wp.colSet(ll, "ok_flag", "!");
        llErr++;
      }
    }

    if (llErr > 0) {
      sqlCommit(-1);
      alertErr("獎金空白: " + llErr);
      return;
    }

    int cnt = -1;
    for (String tmpStr : lsDataRemark1) {
      cnt++;
      wp.colSet(cnt, "ok_flag", "");
      // -option-ON-
      if (checkBoxOptOn(cnt, aaOpt)) {
        continue;
      }

      if (empty(tmpStr)) {
        wp.colSet(cnt, "ok_flag", "!");
        llErr++;
        continue;
      }

      if (cnt != Arrays.asList(lsDataRemark1).indexOf(tmpStr)) {
        wp.colSet(cnt, "ok_flag", "!");
        llErr++;
      }
    }

    if (llErr > 0) {
      sqlCommit(-1);
      alertErr("資料值重複 or 級距空白: " + llErr);
      return;
    }

    String ls_remark = "";
    // --排列大小
    for (int xx = 0; xx < wp.itemRows("data_remark1"); xx++) {
      for (int zz = 0; zz < lsDataRemark1.length; zz++) {
        if (commString.strToNum(lsDataRemark1[xx]) > commString.strToNum(lsDataRemark1[zz])) {
          if (checkBoxOptOn(zz, aaOpt))
            continue;
          if (this.pos(ls_remark, lsDataRemark1[zz]) >= 0)
            continue;
          sqlCommit(-1);
          errmsg("級距順序輸入錯誤 !");
          return;
        }
      }
      ls_remark += lsDataRemark1[xx];
    }

    func.varsSet("valid_date", lsValidDate);
    func.varsSet("param_type", lsParamType);
    func.varsSet("promote_type", lsPromoteType);

    if (func.deleteAllDetl() != 1) {
      errmsg(func.getMsg());
      sqlCommit(-1);
      return;
    }
    int liDataCode = 0;
    for (int ii = 0; ii < wp.itemRows("data_remark1"); ii++) {
      if (checkBoxOptOn(ii, aaOpt))
        continue;
      liDataCode++;
      func.varsSet("data_code1", "" + liDataCode);
      func.varsSet("data_remark1", lsDataRemark1[ii]);
      func.varsSet("point_amt", lsPointAmt[ii]);

      if (func.insertDetl() == 1) {
        llOk++;
        wp.colSet(ii, "ok_flag", "V");
        continue;
      } else {
        llErr++;
        wp.colSet(ii, "ok_flag", "X");
      }

    }

    if (llErr > 0) {
      sqlCommit(-1);
    } else {
      sqlCommit(1);
    }

    if (llErr > 0) {
      alertMsg("存檔失敗");
      return;
    }


    alertMsg("存檔完成");


  }

  @Override
  public void initButton() {
    btnModeAud("XX");
  }

  @Override
  public void initPage() {
    if (eqIgno(wp.respHtml, "mktm5040")) {
      try {
        dataRead();
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      if (wp.colEmpty("ind_num"))
        wp.colSet("ind_num", 0);
    }

  }

}
