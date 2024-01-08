/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-06-22  V1.00.00   Andy             program initial                     *
* 106-12-14                      Andy		     update : ucStr==>commString      *
* 109-01-03  V1.00.01   Justin Wu    updated for archit.  change             *
* 109-04-21  V1.00.02  YangFang   updated for project coding standard        *
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/

package genm01;


import ofcapp.BaseAction;
import taroko.com.TarokoCommon; 

public class Genm0120 extends BaseAction {
  String mExStdVouchCd = "";
  String mExStdVouchDesc = "";
  Genm0120Func func;
  int ilOk = 0;
  int ilErr = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    switch (wp.buttonCode) {
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        wp.setValue("INIT_FLAG", "Y");
        clearFunc();
        break;
      case "S2":
        /* 存檔 */
        strAction = "S2";
        saveFunc();
        break;
      case "AJAX":
        strAction = "AJAX";
        processAjaxOption();
        break;
      default:
        break;
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    try {
      wp.setListCount(0);
      wp.listCount[0] = 1;
      wp.setValue("SER_NUM", "01");
      wp.setValue("INIT_FLAG", "Y");
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    // 設定queryRead() SQL條件
    // 判斷Key是否為空值
    mExStdVouchCd = wp.itemStr("ex_std_vouch_cd");

    wp.whereStr = "where 1=1 ";
    if (empty(mExStdVouchCd) == false) {
      wp.whereStr += " and std_vouch_cd = :std_vouch_cd ";
      setString("std_vouch_cd", mExStdVouchCd);
    } else {
      errmsg("R6分錄代碼不可為空值 ,請重新輸入!");
      wp.listCount[0] = 1;
      wp.setValue("INIT_FLAG", "Y");
      return;
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
    // join gen_sys_vouch & gen_sys_vouch
    wp.selectSQL = " gsv.std_vouch_cd" + " , gsv.std_vouch_desc" + " , gsv.dbcr" + " , gsv.ac_no"
        + " , decode(gd.ac_brief_name,'',gd.ac_full_name,gd.ac_brief_name) as db_brief"
        + " , gsv.memo3_kind" + " , gsv.memo1" + " , gsv.memo2" + " , gsv.memo3" + " , gsv.dbcr_seq"
        + " , uf_2ymd(gsv.mod_time) as mod_date" + " , gsv.mod_user "
        + " , hex(gsv.rowid) as rowid";
    // table name
    wp.daoTable = "gen_sys_vouch  as gsv left join gen_acct_m as gd on gsv.ac_no = gd.ac_no";

    // order column
    wp.whereOrder = " order by gsv.dbcr_seq,gsv.std_vouch_cd,gsv.ac_no,gsv.dbcr desc";


    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      wp.listCount[0] = 1;
      wp.setValue("INIT_FLAG", "Y");
      return;
    }

    wp.totalRows = wp.selectCnt;
    wp.listCount[1] = wp.selectCnt;
    wp.setPageValue();
    listWkdata();


  }

  @Override
  public void querySelect() throws Exception {
    // m_ex_std_vouch_cd =wp.item_ss("std_vouch_cd");
    // dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {

    if (empty(wp.itemStr("ex_std_vouch_cd"))) {
      alertMsg("R6分錄代碼不可空值!!");
      return;
    }

    if (empty(wp.itemStr("std_vouch_desc"))) {
      alertMsg("R6分錄代碼說明不可空值!!");
      return;
    }

    String[] aa_rowid = wp.itemBuff("rowid");
    if (empty(aa_rowid[0])) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from gen_sys_vouch where std_vouch_cd = ? ";
      Object[] param = new Object[] {wp.itemStr("ex_std_vouch_cd")};
      sqlSelect(lsSql, param);
      if (sqlNum("tot_cnt") > 0) {
        errmsg("此R6分錄代碼已被使用，無法新增");
        return;
      }
    }

    func = new Genm0120Func(wp);
    if (strAction.equals("S2")) {

      int llOk = 0, llErr = 0;
      String[] aaAbcr = wp.itemBuff("dbcr");
      String[] aaAcNo = wp.itemBuff("ac_no");
      // String[] aa_memo3_kind = wp.item_buff("memo3_kind");
      String[] aaMemo1 = wp.itemBuff("memo1");
      String[] aaMemo2 = wp.itemBuff("memo2");
      String[] aaMemo3 = wp.itemBuff("memo3");
      String[] aaOpt = wp.itemBuff("opt");
      wp.listCount[0] = aaAbcr.length;

      // -check approve-
      if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
        return;
      }

      // -delete no-approve-
      if (func.dbDelete() < 0) {
        alertErr(func.getMsg());
        sqlCommit(0);
        return;
      }

      // // -insert-
      int seq = 0;
      for (int ll = 0; ll < aaAbcr.length; ll++) {

        seq = ll + 1;

        if (checkBoxOptOn(ll, aaOpt)) {
          continue;
        }

        func.varsSet("aa_dbcr", aaAbcr[ll]);
        func.varsSet("aa_dbcr_seq", intToStr(seq));
        func.varsSet("h_std_vouch_desc", wp.itemStr("std_vouch_desc"));
        func.varsSet("aa_ac_no", aaAcNo[ll]);
        func.varsSet("aa_memo1", aaMemo1[ll]);
        func.varsSet("aa_memo2", aaMemo2[ll]);
        func.varsSet("aa_memo3", aaMemo3[ll]);

        if (func.dbInsert() == 1) {
          llOk++;
        } else {
          llErr++;
        }
        // 有失敗rollback，無失敗commit
        sqlCommit(llOk > 0 ? 1 : 0);
      }

      queryFunc();
      alertMsg("資料存檔處理完成; OK = " + llOk + ", ERR = " + llErr);
      // alert_msg("資料存檔處理完成!");
    }
  }

  public void saveDetail() throws Exception {

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
      //
      // wp.optionKey=m_ex_std_vouch_cd;
      // dddw_list("dddw_std_vouch_cd","gen_sys_vouch", "std_vouch_cd", "std_vouch_desc", "where 1=1
      // group by std_vouch_cd,std_vouch_desc order by std_vouch_cd ");
    } catch (Exception ex) {
    }
  }

  // 改變欄位值顯示
  void listWkdata() {
    String tmpStr = "";
    String[] cde = new String[] {"D", "C"};
    String[] txt = new String[] {"借", "貸"};
    String[] cde1 = new String[] {"1", "2", "3"};
    String[] txt1 = new String[] {"卡號", "ID+身分證號", "YYYYMMDDUBOOOO"};
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // ss =wp.col_ss(ii,"dbcr");
      // wp.col_set(ii,"dbcr", commString.decode(ss, cde, txt));
      tmpStr = wp.colStr(ii, "memo3_kind");
      wp.colSet(ii, "memo3_kind", commString.decode(tmpStr, cde1, txt1));
    }
  }

  public void processAjaxOption() throws Exception {
    wp.varRows = 1000;
    wp.selectSQL = "std_vouch_cd,std_vouch_desc";
    wp.daoTable = "gen_sys_vouch";
    wp.whereStr =
        "where 1=1 and std_vouch_cd like :std_vouch_cd group by std_vouch_cd,std_vouch_desc ";
    wp.orderField = "std_vouch_cd";

    setString("std_vouch_cd", wp.getValue("ex_std_vouch_cd", 0).toUpperCase() + "%");

    pageQuery();

    for (int i = 0; i < wp.selectCnt; i++) {
      wp.addJSON("OPTION_TEXT",
          wp.colStr(i, "std_vouch_cd") + "_" + wp.colStr(i, "std_vouch_desc"));
      wp.addJSON("OPTION_VALUE", wp.colStr(i, "std_vouch_cd"));
    }
    return;
  }

  @Override
  public void procFunc() {}

  @Override
  public void userAction() {}


}
