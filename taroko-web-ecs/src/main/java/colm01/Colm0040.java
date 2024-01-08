/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/06  V1.00.00    phopho     program initial                          *
*  108/12/03  V1.00.01    phopho     set init_button                          *
*  108/12/31  V1.00.02    phopho     add busi.func.ColFunc.f_auth_query()     *
*  109/01/16  V1.00.03    JustinWu   updatable data: all -> acno_flag=1,2, and cancel querying multiple data
*  109-05-06  V1.00.04    Aoyulan       updated for project coding standard   *
** 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
*  112-07-12  V1.00.05   Ryan        帳戶帳號輸入10碼自動補0                                                                                      *  
*  112-11-10  V1.00.06   sunny        修改暫不電催、暫不發催收簡訊 有效期間小於營業日不可存檔                            *  
******************************************************************************/

package colm01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colm0040 extends BaseEdit {
  CommString commString = new CommString();
  Colm0040Func func;

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
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
        // wp.initOption = "--";
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
  public void queryFunc() throws Exception {
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {

  }

  @Override
  public void querySelect() throws Exception {
    // 2020-01-16JusinWu//
    // kk_p_seqno = wp.item_ss("data_k1");

    // if (empty(kk_p_seqno)) {
    // if (getPseqnobyAcctKey() < 0) {
    // return;
    // }
    // }
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    // 2020-01-16JusinWu BEGIN//
    String lsAcctType, lsAcctKey;
    boolean isErr = false;

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
      return;
    }
    if (empty(lsAcctKey)) {
      alertErr("帳戶號碼(key) 不可空白");
      return;
    }
    if (lsAcctKey.length() < 8) {
      alertErr("[帳戶號碼]輸入至少8碼!");
      return;
    }

    // 2020-01-16JusinWu END//

    // 查詢權限檢查，參考【f_auth_query】

    // //2020-01-16 JustinWu
    // ls_sql = "select uf_acno_key(:p_seqno) as acct_key from dual ";
    // setString("p_seqno", kk_p_seqno);
    // sqlSelect(ls_sql);
    // if (sql_nrow > 0) {
    // ls_acct_key = sql_ss("acct_key");
    // }

    // if (!f_auth_query(parent.classname(),ls_acct_key)) { return -1; }
    busi.func.ColFunc func = new busi.func.ColFunc();
    func.setConn(wp);
    if (func.fAuthQuery(lsAcctKey) != 1) {
      alertErr2(func.getMsg());
      return;
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, "
        // + "p_seqno, "
        + "acno_p_seqno as acno_p_seqno, " + "acno_flag, " + "acct_type, " + "acct_key, "
        + "acct_status, " + "no_tel_coll_flag, " + "no_tel_coll_s_date, " + "no_tel_coll_e_date, "
        + "no_tel_coll_s_date as src_no_tel_coll_s_date, " + "no_sms_flag, " + "no_sms_s_date, "
        + "no_sms_e_date, " + "no_sms_s_date as src_no_sms_s_date, "
        + "uf_corp_name(corp_p_seqno) as corp_name, " + "uf_idno_name(id_p_seqno) as id_name, "
        + "crt_user, " + "crt_date, " + "mod_user, " + "uf_2ymd(mod_time) as mod_date ";

    wp.daoTable = " act_acno ";

    // wp.whereStr = " where p_seqno = :p_seqno ";

    // //2020-01-16 JustinWu
    // wp.whereStr = " where acno_p_seqno = :p_seqno ";
    wp.whereStr = " where acct_type = :acct_type " + " and acct_key = :acct_key ";
    // setString("p_seqno", kk_p_seqno);
    setString("acct_type", lsAcctType);
    setString("acct_key", lsAcctKey);

    pageSelect();

    // // 2020-01-16JusinWu BEGIN//
    // if (sql_notFind()) {
    // alert_err("查無資料, p_seqno:" + kk_p_seqno);
    // return;
    // }

    if (sqlNotFind()) {
      isErr = true;
      alertErr("查無資料: acct_type=" + lsAcctType + ", acct_key=" + lsAcctKey);
    } else {
      if (wp.colStr("acno_flag").equalsIgnoreCase("1")
          || wp.colStr("acno_flag").equalsIgnoreCase("2")) {
        isErr = false;
      } else {
        isErr = true;
        alertErr("此為商務卡個卡，請輸入公司統編進行維護");
      }
    }

    if (isErr) {
      wp.colClear(0, "acct_type");
      wp.colClear(0, "acct_key");
      wp.colClear(0, "chi_name");
      wp.colClear(0, "tt_acct_status");
      wp.colClear(0, "acct_status");
      wp.colClear(0, "no_tel_coll_flag");
      wp.colClear(0, "no_tel_coll_s_date");
      wp.colClear(0, "no_tel_coll_e_date");
      wp.colClear(0, "no_sms_flag");
      wp.colClear(0, "no_sms_s_date");
      wp.colClear(0, "no_sms_e_date");
      wp.colClear(0, "rowid");
      wp.colClear(0, "acno_p_seqno");
      return;
    }
    // // 2020-01-16JusinWu END//

    detlWkdata();

  }

  // // 2020-01-16 JustinWu:取消顯示多筆
  // int getPseqnobyAcctKey() throws Exception {
  // String ls_acct_type, ls_acct_key;
  //
  // wp.col_set("queryReadCnt", "0");
  // //以輸入欄位優先查詢
  // ls_acct_type = wp.item_ss("kk_acct_type");
  // ls_acct_key = wp.item_ss("kk_acct_key");
  // if (empty(ls_acct_type) && empty(ls_acct_key)) {
  // ls_acct_type = wp.item_ss("acct_type");
  // ls_acct_key = wp.item_ss("acct_key");
  // }
  //
  // if (empty(ls_acct_type)) {
  // alert_err("帳戶號碼(type) 不可空白");
  // return -1;
  // }
  // if (empty(ls_acct_key)) {
  // alert_err("帳戶號碼(key) 不可空白");
  // return -1;
  // }
  // if (ls_acct_key.length()<8) {
  // alert_err("[帳戶號碼]輸入至少8碼!");
  // return -1;
  // }
  //
  // daoTid ="P-";
  //// wp.sqlCmd = "select act_acno.p_seqno, act_acno.acct_type, act_acno.acct_key,
  // act_acno.acct_status, "
  // wp.sqlCmd = "select "
  // + "aa.acno_p_seqno as p_seqno, "
  // + "aa.acno_flag, "
  // + "aa.acct_type, "
  // + "aa.acct_key, "
  // + "aa.acct_status, "
  // + "decode(aa.acct_status,'1','1.正常','2','2.逾放','3','3.催收','4','4.呆帳','5','5.結清',aa.acct_status)
  // as tt_acct_status, "
  // + "ci.id_no, "
  // + "ci.id_no_code, "
  // + "ci.chi_name, "
  // + "ci.birthday "
  // + "from act_acno as aa "
  // + "left join crd_idno as ci on aa.id_p_seqno = ci.id_p_seqno "
  // + "where acct_type = :acct_type "
  // + "and acct_key = :acct_key "
  // + "order by aa.acct_type, aa.acct_key ";
  // setString("acct_type", ls_acct_type);
  // setString("acct_key", ls_acct_key);
  //
  // pageQuery();
  //
  // wp.setListCount(1); //彈跳視窗
  // if (sql_nrow == 0) {
  // alert_err("查無資料: acct_type=" + ls_acct_type+", acct_key=" + ls_acct_key);
  // return -1;
  // } else if(sql_nrow == 1){
  // if (! wp.col_ss("p-acno_flag").equalsIgnoreCase("1") &&
  // ! wp.col_ss("p-acno_flag").equalsIgnoreCase("2")) {
  // alert_err("此為商務卡個卡，請輸入公司統編進行維護");
  // return -1;
  // }
  // kk_p_seqno = wp.col_ss("p-p_seqno");
  // return 1;
  // } else {
  // //顯示多選一的彈出視窗畫面
  // wp.setPageValue();
  // wp.col_set("queryReadCnt", int_2Str(sql_nrow));
  // return -1;
  // }
  // }

  void detlWkdata() {
    String acctStatus = wp.colStr("acct_status");
    String[] cde = new String[] {"1", "2", "3", "4", "5"};
    String[] txt = new String[] {"1.正常", "2.逾放", "3.催收", "4.呆帳", "5.結清"};
    wp.colSet("tt_acct_status", commString.decode(acctStatus, cde, txt));

    // ss=wp.col_ss("chi_name");
    // if (empty(ss))
    wp.colSet("chi_name",
        empty(wp.colStr("id_name")) ? wp.colStr("corp_name") : wp.colStr("id_name"));

    // //2020-01-16: 取消預設起日
    // //問題單 0001213: 1.維護起日預設當日
    // ss=wp.col_ss("no_tel_coll_s_date");
    // if (empty(ss)) {
    // wp.col_set("no_tel_coll_s_date", wp.sysDate);
    // }
    //
    // ss=wp.col_ss("no_sms_s_date");
    // if (empty(ss)) {
    // wp.col_set("no_sms_s_date", wp.sysDate);
    // }

  }

  @Override
  public void saveFunc() throws Exception {

    func = new Colm0040Func(wp);

    if (ofcUpdatebefore1() < 0)
      return;

    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
    if (rc == 1) {
      wp.colSet("src_no_tel_coll_s_date", wp.itemStr("no_tel_coll_s_date"));
      wp.colSet("src_no_sms_s_date", wp.itemStr("no_sms_s_date"));
    }
  }


  int ofcUpdatebefore1() throws Exception {
    String lsSql = "", lsBusinessDate = "", tellFlag = "", smsFlag = "";
    String sTellDate = "", eTellDate = "", srcTellDate = "", sSmsDate = "", eSmsDate = "",
        srcSmsDate = "";

    sTellDate = wp.itemStr("no_tel_coll_s_date");
    eTellDate = wp.itemStr("no_tel_coll_e_date");
    srcTellDate = wp.itemStr("src_no_tel_coll_s_date");
    tellFlag = wp.itemStr("no_tel_coll_flag");
    if (isEmpty(sTellDate) && !isEmpty(eTellDate)) {
      alertErr("[暫不電催-有效期間]  起日不可空白");
      return -1;
    }
    if (chkStrend(sTellDate, eTellDate) == false) {
      alertErr("[暫不電催-有效期間]  輸入錯誤");
      return -1;
    }

    sSmsDate = wp.itemStr("no_sms_s_date");
    eSmsDate = wp.itemStr("no_sms_e_date");
    srcSmsDate = wp.itemStr("src_no_sms_s_date");
    smsFlag = wp.itemStr("no_sms_flag");
    if (isEmpty(sSmsDate) && !isEmpty(eSmsDate)) {
      alertErr("[暫不發簡訊-有效期間]  起日不可空白");
      return -1;
    }
    if (chkStrend(sSmsDate, eSmsDate) == false) {
      alertErr("[暫不發簡訊-有效期間]  輸入錯誤");
      return -1;
    }

    // 1. 檢查日期不得小於營業日
    lsSql = "select business_date from ptr_businday ";
    sqlSelect(lsSql);
    lsBusinessDate = sqlStr("business_date");

    // sb_date=string(dec(ls_business_date)-19110000)
    // sa_date=string(dec(trim(dw_data.of_getitem(1,'recourse_mark_date')))-19110000)
    // sb_date = ls_business_date;

    if (eqIgno(sTellDate, srcTellDate) == false) {
      if (notEmpty(sTellDate)) {
        // if ( to_Num(sn_date) < to_Num(sb_date) ) {
        //if (chkStrend(sTellDate, lsBusinessDate)) {
         if (sTellDate.compareTo(lsBusinessDate)<0) {
          alertErr("暫不電催之有效期間不得小於營業日(" + lsBusinessDate + ")");
          return -1;
        }
        tellFlag = "Y";
      } else {
        tellFlag = "N";
      }
    }
    if (eqIgno(sSmsDate, srcSmsDate) == false) {
      if (notEmpty(sSmsDate)) {
        // if ( to_Num(ss_date) < to_Num(sb_date) ) {
//        if (chkStrend(sSmsDate, lsBusinessDate)) {
    	  if (sSmsDate.compareTo(lsBusinessDate)<0) {
          alertErr("暫不發簡訊之有效期間不得小於營業日(" + lsBusinessDate + ")");
          return -1;
        }
        smsFlag = "Y";
      } else {
        smsFlag = "N";
      }
    }
    wp.colSet("no_tel_coll_flag", tellFlag);
    wp.colSet("no_sms_flag", smsFlag);
    func.varsSet("no_tel_coll_flag", tellFlag);
    func.varsSet("no_sms_flag", smsFlag);

    return 1;
  }


  @Override
  public void initButton() {
    // if (wp.respHtml.indexOf("_detl")>0) {
    this.btnModeAud();
    // }
  }

}
