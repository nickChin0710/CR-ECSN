/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-06-24  V1.00.00  yash       program initial                            *
* 108-08-08  V1.00.01  Andy       remove col ica_no                          *
* 108-12-30  V1.00.02 JustonWu change if-else in actionFunction() to switch-case
* 109-04-20  V1.00.03  Tanwei       updated for project coding standard     *
* 109-12-23  V1.00.04  Justin      chg var names
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
* 111-12-08  V1.00.05   Ryan       增加EPAY_TPAN_CODE欄位                                                              *    
******************************************************************************/
package ptrm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;


public class Ptrm0040 extends BaseEdit {
  Ptrm0040Func func;

  String binNo = "";
  //String kk2 = "";
  String tmpBinNo2Fm = "";
  String tmpBinNo2To = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;
    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
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
      default:
        break;
    }

    dddwSelect();// 資料庫bin_no所有資料在HTML的select標籤內顯示
    initButton();// 資料庫curr_code所有資料在HTML的select標籤內顯示
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_bin_no")) == false) {// wp.item_ss("ex_bin_no") = null 則回傳ture
      wp.whereStr += " and  bin_no = :bin_no ";// bin_no = :bin_no===>輸入的資料內容與bin_no內的資料作比對
      setString("bin_no", wp.itemStr("ex_bin_no"));// (HashMap),將wp.item_ss("ex_bin_no")放入bin_no(key)中
    }
    if (empty(wp.itemStr("ex_bin_type")) == false) {
      wp.whereStr += " and  bin_type = :bin_type ";
      setString("bin_type", wp.itemStr("ex_bin_type"));
    }
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();// 執行SQL查詢
  }

  //111-12-08  V1.00.05   Ryan       增加EPAY_TPAN_CODE欄位                                                           
  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " bin_no, " + "bin_type, " + "ica_no," + "debit_flag," + " dc_curr_code, "
        + " card_desc, " + " bin_no_2_fm," + " bin_no_2_to," + " card_type," + " crt_date, "
        + " crt_user, " + " crt_time, " + " mod_time, " + " mod_user ,epay_tpan_code";

    wp.daoTable = "ptr_bintable";// Table名稱
    wp.whereOrder = " order by bin_no,bin_no_2_fm,bin_no_2_to";// order by bin_no===>依據bin_no排序

    getWhereStr();
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // list_wkdata();
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {// html點選單筆資料後做查詢，在detl畫面顯示
    binNo = wp.itemStr("bin_no");
    tmpBinNo2Fm = wp.itemStr("bin_no_2_fm");
    tmpBinNo2To = wp.itemStr("bin_no_2_to");
    dataRead();
  }
  
 //111-12-08  V1.00.05   Ryan       增加EPAY_TPAN_CODE欄位       
  @Override
  public void dataRead() throws Exception {// 在detl畫面做查詢
    binNo = wp.itemStr("kk_bin_no");
    tmpBinNo2Fm = wp.itemStr("kk_bin_no_2_fm");
    tmpBinNo2To = wp.itemStr("kk_bin_no_2_to");
    if (empty(binNo)) {
      binNo = itemKk("data_k1");
    }
    if (empty(tmpBinNo2Fm)) {
    	tmpBinNo2Fm = itemKk("data_k2");
    }
    if (empty(tmpBinNo2To)) {
    	tmpBinNo2To = itemKk("data_k3");
    }

    if (empty(binNo)) {
      binNo = wp.colStr("bin_no");
    }
    if (empty(tmpBinNo2Fm)) {
    	tmpBinNo2Fm = wp.colStr("bin_no_2_fm");
    }
    if (empty(tmpBinNo2To)) {
    	tmpBinNo2To = wp.colStr("bin_no_2_to");
    }

    // if (isEmpty(wp.item_ss("bin_no"))){
    // alert_err("BIN NO : 不可空白");
    // return;
    // }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + " bin_no, " + "bin_type, " + "ica_no,"
        + "debit_flag," + " dc_curr_code," + " card_desc," + " bin_no_2_fm," + " bin_no_2_to,"
        + " card_type," + " crt_date," + " crt_user," + " crt_time,"
        + " uf_2ymd(mod_time) as mod_date," + " mod_user ,epay_tpan_code";
    wp.daoTable = "ptr_bintable";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  bin_no = :bin_no ";
    setString("bin_no", binNo);
    wp.whereStr += " and  bin_no_2_fm = :bin_no_2_fm ";
    setString("bin_no_2_fm", tmpBinNo2Fm);
    wp.whereStr += " and  bin_no_2_to = :bin_no_2_to ";
    setString("bin_no_2_to", tmpBinNo2To);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key = " + binNo + " 7-12碼起 = " + tmpBinNo2Fm + " 7-12碼迄 = " + tmpBinNo2To);
    }
  }

  @Override
  public void saveFunc() throws Exception {// 新增、修改、刪除

    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }


    func = new Ptrm0040Func(wp);


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
		if (wp.colEq("btnUpdate_disable", "disabled")) {
			wp.colSet("input_bin_type_disable", "disabled");
		}else {
			wp.colSet("select_bin_type_disable", "disabled");
		}
    }
  }


  @Override
  public void dddwSelect() {
    try {
      wp.optionKey = wp.itemStr("ex_bin_no");// 接HTML的name值(name= ex_bin_no)
      this.dddwList("dddw_bin_no", "ptr_bintable", "distinct bin_no", "", "where 1=1");// where 1=1 =
                                                                              // true條件成立，將bin_no所有資料顯示HTML(前端抓取dddw_bin_no)
      wp.optionKey = wp.colStr("dc_curr_code");
      this.dddwList("dddw_dc_curr_code", "Ptr_currcode", "curr_code",
          "curr_code||'['||curr_eng_name||']'||curr_chi_name", "where 1=1");

      wp.initOption = "--";
      wp.optionKey = wp.colStr("card_type");
      this.dddwList("dddw_card_type", "ptr_card_type", "card_type", "name",
          " where  1=1  order by card_type");

    } catch (Exception ex) {
    }
  }

}
