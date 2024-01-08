/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/06  V1.00.00    phopho     program initial                          *
*  108/12/03  V1.00.01    phopho     set init_button                          *
*  108/12/31  V1.00.02    phopho     add busi.func.ColFunc.f_auth_query()     *
*  109/01/20  V1.00.03    JustinWu   updatable data: all -> acno_flag=1,2, and cancel querying multiple data
*  109-05-06  V1.00.04    Aoyulan       updated for project coding standard   *
** 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
** 112-07-12  V1.00.05   Ryan        帳戶帳號輸入10碼自動補0                                                                                      *  
** 112-10-17  V1.00.06   Ryan        修正客戶名稱                                                                                      *  
** 112-11-10  V1.00.07   Ryan        修改暫不轉逾放,暫不轉催收 有效期間小於營業日不可存檔                            *  
******************************************************************************/

package colm01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colm0050 extends BaseEdit {
  CommString commString = new CommString();
  Colm0050Func func;

  String kkAcnoPSeqno = "";
  String kkOptname = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml="
        + wp.respHtml);
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        // String exID = wp.item_ss("exID");
        clearFunc();
        // wp.col_set("kk_acct_key", exID);
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "R":
        // -資料讀取-
        strAction = "R";
        querySelect();
        // if (wp.item_ss("dataType").equals("0")) {
        // dataRead();
        // }else{
        // dataRead3();
        // }
        break;
      case "A":
        /* 新增功能 */
        insertFunc();
        break;
      case "U":
        /* 更新功能 */
        updateFunc();
        // if (wp.item_ss("dataType").equals("0")) {
        // updateFunc();
        // }else{
        // updateFunc2();
        // }
        break;
      case "D":
        /* 刪除功能 */
        deleteFunc();
        // if (wp.item_ss("dataType").equals("0")) {
        // deleteFunc();
        // }else{
        // deleteFunc2();
        // }
        break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        wp.colSet("queryReadCnt", "0");
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

    dddwSelect();
    initButton();
  }


  // public void updateFunc2() throws Exception {
  // saveFunc2();
  // if (rc == 1) {
  // if (updateRetrieve) {
  // dataRead2();
  // } else {
  // mod_seqno_add();
  // }
  // }
  // }
  // public void deleteFunc2() throws Exception {
  // saveFunc2();
  // if (rc == 1) {
  // clearFunc();
  // }
  // }

  @Override
  public void queryFunc() throws Exception {
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {

  }


  @Override
  public void querySelect() throws Exception {
    // // 2020-01-20JusinWu//
    // kk_acno_p_seqno = wp.item_ss("data_k1");

    if (empty(kkAcnoPSeqno)) {
      if (getPseqnobyAcctKey() < 0) {
        // 清除先前查詢結果
        wp.colClear(0, "acct_type");
        wp.colClear(0, "acct_key");
        wp.colClear(0, "chi_name");
        wp.colClear(0, "tt_acct_status");
        wp.colClear(0, "acct_status");
        wp.colClear(0, "no_delinquent_flag");
        wp.colClear(0, "no_delinquent_s_date");
        wp.colClear(0, "no_delinquent_e_date");
        wp.colClear(0, "no_collection_flag");
        wp.colClear(0, "no_collection_s_date");
        wp.colClear(0, "no_collection_e_date");
        wp.colClear(0, "crt_date");
        wp.colClear(0, "crt_user");
        wp.colClear(0, "rowid");
        wp.colClear(0, "aud_code");
        wp.colClear(0, "acno_p_seqno");
        return;
      }
    }
    dataRead();
  }


  int getPseqnobyAcctKey() throws Exception {
    String lsAcctType, lsAcctKey;
    int returnValue;

    wp.colSet("queryReadCnt", "0");
    // 以輸入欄位優先查詢
    lsAcctType = wp.itemStr("kk_acct_type");
    lsAcctKey = wp.itemStr("kk_acct_key");
    if (empty(lsAcctType) && empty(lsAcctKey)) {
      lsAcctType = wp.itemStr("acct_type");
      lsAcctKey = wp.itemStr("acct_key");
    }
    
    if(lsAcctKey.length()==10) {
    	lsAcctKey = lsAcctKey + "0";
    }
    
    if (empty(lsAcctType)) {
      alertErr("帳戶號碼(type) 不可空白");
      return -1;
    }
    if (empty(lsAcctKey)) {
      alertErr("帳戶號碼(key) 不可空白");
      return -1;
    }
    if (lsAcctKey.length() < 8) {
      alertErr("[帳戶號碼]輸入至少8碼!");
      return -1;
    }

    daoTid = "P-";
    // wp.sqlCmd = "select act_acno.p_seqno, act_acno.acct_type, act_acno.acct_key,
    // act_acno.acct_status, "
    wp.sqlCmd = "select aa.acno_p_seqno as acno_p_seqno, " + "aa.acct_type, " + "aa.acct_key, "
        + "aa.acct_status, " + "aa.acno_flag, "
        + "decode(aa.acct_status,'1','1.正常','2','2.逾放','3','3.催收','4','4.呆帳','5','5.結清',aa.acct_status) as tt_acct_status, "
        + "ci.id_no, " + "ci.id_no_code, " + "ci.chi_name, " + "ci.birthday "
        + "from act_acno as aa " + "left join crd_idno as ci on aa.id_p_seqno = ci.id_p_seqno "
        + "where acct_type = :acct_type " + "and acct_key = :acct_key "
        + "order by aa.acct_type, aa.acct_key ";
    setString("acct_type", lsAcctType);
    setString("acct_key", lsAcctKey);

    pageQuery();

    wp.setListCount(1); // 彈跳視窗
    if (sqlRowNum == 1) {
      kkAcnoPSeqno = wp.colStr("p-acno_p_seqno");
      // 2020-01-20 Justin Wu
      // 若acno_flag等於1或2，則可查詢及異動
      if (wp.colStr("p-acno_flag").equalsIgnoreCase("1")
          || wp.colStr("p-acno_flag").equalsIgnoreCase("2")) {
        returnValue = 1;
      } else {
        alertErr("此為商務卡個卡，請輸入公司統編進行維護");
        returnValue = -1;
      }
    } else {
      alertErr("查無資料: acct_type=" + lsAcctType + ", acct_key=" + lsAcctKey);
      returnValue = -1;
    }
    return returnValue;

    // // 2020-01-20 JustinWu: cancel querying multiple data
    // else {
    // //顯示多選一的彈出視窗畫面
    // wp.setPageValue();
    // wp.col_set("queryReadCnt", int_2Str(sql_nrow));
    // return -1;
    // }

  }

  @Override
  public void dataRead() throws Exception {
    String lsSql = "", lsAcctKey = "";

    lsSql = "select uf_acno_key(:acno_p_seqno) as acct_key from dual ";
    setString("acno_p_seqno", kkAcnoPSeqno);
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      lsAcctKey = sqlStr("acct_key");
    }

    // if (!f_auth_query(parent.classname(),ls_acct_key)) {
    // return -1;
    // }

    // 查詢權限檢查，參考【f_auth_query】
    busi.func.ColFunc func = new busi.func.ColFunc();
    func.setConn(wp);
    if (func.fAuthQuery(lsAcctKey) != 1) {
      alertErr2(func.getMsg());
      return;
    }

    wp.selectSQL = "hex(rowid) as rowid, " + "mod_seqno, " + "p_seqno as acno_p_seqno, "
        + "acct_type, " + "uf_acno_key(p_seqno) as acct_key, " + "acct_status, " + "chi_name, "
        + "no_delinquent_flag, " + "no_delinquent_s_date, " + "no_delinquent_e_date, "
        + "no_delinquent_s_date as src_no_delinquent_s_date, " + "no_collection_flag, "
        + "no_collection_s_date, " + "no_collection_e_date, "
        + "no_collection_s_date as src_no_collection_s_date, " + "crt_user, " + "crt_date, "
        + "mod_user, " + "uf_2ymd(mod_time) as mod_date, " + "'U' as aud_code ";

    wp.daoTable = " col_acno_t ";
    wp.whereStr = " where p_seqno = :acno_p_seqno ";
    setString("acno_p_seqno", kkAcnoPSeqno); // p_seqno in table COL_ACNO_T == acno_p_seqno in table
                                             // ACT_ACNO

    pageSelect();
    if (sqlNotFind()) {
      wfActAcno();
    }

    detlWkdata();
  }

  // 執行【queryColAcnoR】，無資料之邏輯處理。
  int wfActAcno() throws Exception {

    wp.selectSQL = " hex(rowid) as rowid ," + "'0' as mod_seqno "
    // + " ,p_seqno "
        + " ,acno_p_seqno " + " ,acct_type " + " ,acct_key " + " ,acct_status " + " ,corp_p_seqno "
        + " ,id_p_seqno " + " ,uf_corp_name(corp_p_seqno) corp_name "
        + " ,uf_idno_name(id_p_seqno) id_name " + " ,no_delinquent_flag "
        + " ,no_delinquent_s_date " + " ,no_delinquent_e_date "
        + " ,no_delinquent_s_date as src_no_delinquent_s_date " + " ,no_collection_flag "
        + " ,no_collection_s_date " + " ,no_collection_e_date "
        + " ,no_collection_s_date as src_no_collection_s_date " + " ,'A' as aud_code ";

    wp.daoTable = " act_acno ";
    // wp.whereStr = " where p_seqno = :p_seqno ";
    wp.whereStr = " where acno_p_seqno = :acno_p_seqno ";
    setString("acno_p_seqno", kkAcnoPSeqno);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, 帳戶流水號:" + kkAcnoPSeqno);
      return -1;
    }

    wp.colSet("pho_disable", "disabled");
    return 1;
  }

  void detlWkdata() {
    String acctStatus = wp.colStr("acct_status");
    String[] cde = new String[] {"1", "2", "3", "4", "5"};
    String[] txt = new String[] {"1.正常", "2.逾放", "3.催收", "4.呆帳", "5.結清"};
    wp.colSet("tt_acct_status", commString.decode(acctStatus, cde, txt));

    // ss=wp.col_ss("chi_name");
    // if (empty(ss))
//    if (wp.colEmpty("chi_name")) {
      wp.colSet("chi_name",
          empty(wp.colStr("id_name")) ? wp.colStr("corp_name") : wp.colStr("id_name"));
    }
//  }

  @Override
  public void saveFunc() throws Exception {
    func = new Colm0050Func(wp);

    if (ofcUpdatebefore2() < 0)
      return;

    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
    if (rc == 1) {
      wp.colSet("src_no_delinquent_s_date", wp.itemStr("no_delinquent_s_date"));
      wp.colSet("src_no_collection_s_date", wp.itemStr("no_collection_s_date"));
      wp.colSet("aud_code", "U");
    }
  }

  int ofcUpdatebefore2() throws Exception {
    String lsSql = "", lsBusinessDate = "", lsAcctStatus = "", lsTransType = "", nFlag = "",
        sFlag = "";
    String saDate = "", seDate = "", srcDate = "", ssDate = "", eeDate = "", sscDate = "",
        sbDate = "";

    kkAcnoPSeqno = wp.itemStr("acno_p_seqno");
    lsAcctStatus = wp.itemStr("acct_status");

    saDate = wp.itemStr("no_delinquent_s_date");
    seDate = wp.itemStr("no_delinquent_e_date");
    srcDate = wp.itemStr("src_no_delinquent_s_date");
    nFlag = wp.itemStr("no_delinquent_flag");
    if (isEmpty(saDate) && !isEmpty(seDate)) {
      alertErr("[暫不逾放-有效期間]  起日不可空白");
      return -1;
    }
    if (chkStrend(saDate, seDate) == false) {
      alertErr("[暫不逾放-有效期間]  輸入錯誤");
      return -1;
    }
    // if(!isEmpty(sa_date)){
    // NT = "Y";
    // }else{
    // NT = isEmpty(wp.item_ss("no_delinquent_flag"))? "":"N";
    // }

    ssDate = wp.itemStr("no_collection_s_date");
    eeDate = wp.itemStr("no_collection_e_date");
    sscDate = wp.itemStr("src_no_collection_s_date");
    sFlag = wp.itemStr("no_collection_flag");
    if (isEmpty(ssDate) && !isEmpty(eeDate)) {
      alertErr("[暫不逾放-有效期間]  起日不可空白");
      return -1;
    }
    if (chkStrend(ssDate, eeDate) == false) {
      alertErr("[暫不逾放-有效期間]  輸入錯誤");
      return -1;
    }
    // if(!isEmpty(ss_date)){
    // NS = "Y";
    // }else{
    // NS = isEmpty(wp.item_ss("no_collection_flag"))? "":"N";
    // }

    if ((notEmpty(saDate)) && (eqIgno(lsAcctStatus, "3"))) {
      alertErr("催收戶不可轉暫不逾放");
      return -1;
    }
    if ((notEmpty(saDate)) && (eqIgno(lsAcctStatus, "4"))) {
      alertErr("呆帳戶不可轉暫不逾放");
      return -1;
    }
    if ((notEmpty(ssDate)) && (eqIgno(lsAcctStatus, "3"))) {
      alertErr("催收戶不可轉暫不催收");
      return -1;
    }
    if ((notEmpty(ssDate)) && (eqIgno(lsAcctStatus, "4"))) {
      alertErr("呆帳戶不可轉暫不催收");
      return -1;
    }

    if (notEmpty(ssDate)) {
      lsSql = "select trans_type from col_wait_trans where p_seqno = :acno_p_seqno ";
      setString("acno_p_seqno", kkAcnoPSeqno);
      sqlSelect(lsSql);
      if (sqlRowNum > 0)
        lsTransType = sqlStr("trans_type");

      if (eqIgno(lsTransType, "3")) {
        wp.colSet("no_collection_s_date", "");
        wp.colSet("no_collection_e_date", "");
        alertErr("該戶轉催收覆核中, 不可暫不轉催收");
        return -1;
      }
    }

    // 取得營業日
    lsSql = "select business_date from ptr_businday ";
    sqlSelect(lsSql);
    lsBusinessDate = sqlStr("business_date");

    // sb_date=string(dec(ls_business_date)-19110000)
    // sa_date=string(dec(trim(dw_data.of_getitem(1,'recourse_mark_date')))-19110000)
    sbDate = lsBusinessDate;

    if (eqIgno(saDate, srcDate) == false) {
      if (notEmpty(saDate)) {
        // if ( to_Num(sa_date) < to_Num(sb_date) ) {
        if (saDate.compareTo(sbDate)<0) {
          alertErr("暫不逾放之有效期間不得小於營業日(" + sbDate + ")");
          return -1;
        }
        nFlag = "Y";
      } else {
        nFlag = "N";
      }
    }
    if (eqIgno(ssDate, sscDate) == false) {
      if (notEmpty(ssDate)) {
        // if ( to_Num(ss_date) < to_Num(sb_date) ) {
        if (ssDate.compareTo(sbDate)<0) {
          alertErr("暫不催收之有效期間不得小於營業日(" + sbDate + ")");
          return -1;
        }
        sFlag = "Y";
      } else {
        sFlag = "N";
      }
    }
    wp.colSet("no_delinquent_flag", nFlag);
    wp.colSet("no_collection_flag", sFlag);
    func.varsSet("no_delinquent_flag", nFlag);
    func.varsSet("no_collection_flag", sFlag);

    return 1;
  }

  // public void updateFunc2() throws Exception {
  // saveFunc2();
  // if (rc == 1) {
  // if (updateRetrieve) {
  // dataRead2();
  // } else {
  // mod_seqno_add();
  // }
  // }
  // }
  // public void deleteFunc2() throws Exception {
  // saveFunc2();
  // if (rc == 1) {
  // clearFunc();
  // }
  // }

  @Override
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("kk_acct_type");
      } else if (!wp.itemEmpty("kk_acct_type")) {
        wp.optionKey = wp.itemStr("kk_acct_type");
      } else {
        wp.optionKey = wp.itemStr("exAcctType");
      }
      this.dddwList("PtrAcctTypeList", "ptr_acct_type", "acct_type",
          "acct_type||' ['||chin_name||']'", "where 1=1 order by acct_type");

    } catch (Exception ex) {
    }
  }


  @Override
  public void initButton() {
    // if (wp.respHtml.indexOf("_detl")>0) {
    this.btnModeAud();
    // }
  }

}
