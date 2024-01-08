/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-03  V1.00.01  ryan       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 108-08-21  V1.00.02  Andy		  update                                     *
* 108-09-16  V1.00.03  Andy       update :add item eleconic_code             *
* 108-12-19  V1.00.04  Andy       update :add new card_no check              *
* 108-12-19  V1.00.04  ryan       update :ptr_branch==>gen_brn               *
* 108-12-23  V1.00.05  ryan       update :delete err                         *  
* 109-03-05  V1.00.06  Wilson     調整還原指定卡號邏輯                                                                                 * 
* 109-03-16  V1.00.07  Wilson     card_flag = '1'                            *
* 109-04-09  V1.00.08  Wilson     post_flag = 'Y'                            * 
* 109-04-13  V1.00.09  Wilson     寄件別預設                                                                                                    *
* 109-04-28  V1.00.10  YangFang   updated for project coding standard        * 
* 109-01-04  V1.00.11   shiyuqi       修改无意义命名                                                                                   *
* 112-02-02  V1.00.12  Wilson    移除delete crd_seqno_log error               *  
* 112-04-22  V1.00.13  Wilson    mark檢核晶片效期                                                                                        *
* ****************************************************************************/
package crdm01;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import busi.SqlPrepare;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Crdm0130 extends BaseEdit {
  Crdm0130Func func;
  int i = 0, iiUnit = 0;
  String kk1Batchno = "";
  String kk2Recno = "";
  String isCardno = "", isAudcode = "";

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
    } else if (eqIgno(wp.buttonCode, "S2")) {
      strAction = "S2";
      StringBuffer str = new StringBuffer();
      str.append("where 1=1 ");
      str.append("and reissue_reason = '");
      str.append(wp.itemStr("emboss_reason"));
      str.append("' order by reissue_code");
      wp.optionKey = "";
      dddwSelect2(str.toString());
    } else if (eqIgno(wp.buttonCode, "R2")) {
      // -資料讀取-
      strAction = "R2";
      if (ofcPreretrieve() == 1) {
        wfAardnoAata(isCardno);
      }
    } else if (eqIgno(wp.buttonCode, "R3")) {
      // 認同集團碼中文
      strAction = "R3";
      selectCrdNcccCard(wp.itemStr("unit_code"), wp.itemStr("card_type"));
    }

    dddwSelect();
    initButton();
  }


  @Override
  public void initPage() {
    wp.colSet("crt_user", wp.loginUser);
    wp.colSet("crt_date", getSysDate());
    wp.colSet("mail_type", "1");
    // if (wp.respHtml.indexOf("_detl") > 0) {
    // String ls_sql ="";
    // ls_sql = "select "
    // }
    //
  }

  int getWhereStr() {
    wp.whereStr = " where 1=1 and emboss_source = '5' ";

    if (empty(wp.itemStr("ex_batchno")) &&
    // empty(wp.item_ss("ex_recno"))&&
        empty(wp.itemStr("ex_date1")) && empty(wp.itemStr("ex_date2"))
        && empty(wp.itemStr("ex_old_card_no")) && empty(wp.itemStr("ex_chi_name"))
        && empty(wp.itemStr("ex_corp_no")) && empty(wp.itemStr("ex_apply_id"))
        && empty(wp.itemStr("ex_emboss_reason")) && empty(wp.itemStr("ex_crt_user"))) {
      alertErr("至少輸入一個查詢條件");
      return -1;
    }

    if (empty(wp.itemStr("ex_batchno")) == false) {
      wp.whereStr += " and  batchno = :ex_batchno ";
      setString("ex_batchno", wp.itemStr("ex_batchno"));
    }
    // 20190821 stop use
    // if (empty(wp.item_ss("ex_recno")) == false) {
    // wp.whereStr += " and recno = :ex_recno ";
    // setString("ex_recno", wp.item_ss("ex_recno"));
    // }
    wp.whereStr += sqlStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2"), "crt_date");

    if (empty(wp.itemStr("ex_old_card_no")) == false) {
      wp.whereStr += " and  old_card_no = :ex_old_card_no ";
      setString("ex_old_card_no", wp.itemStr("ex_old_card_no"));
    }
    if (empty(wp.itemStr("ex_chi_name")) == false) {
      wp.whereStr += " and  chi_name = :ex_chi_name ";
      setString("ex_chi_name", wp.itemStr("ex_chi_name"));
    }
    if (empty(wp.itemStr("ex_corp_no")) == false) {
      wp.whereStr += " and  corp_no = :ex_corp_no ";
      setString("ex_corp_no", wp.itemStr("ex_corp_no"));
    }
    if (empty(wp.itemStr("ex_apply_id")) == false) {
      wp.whereStr += " and  apply_id = :ex_apply_id ";
      setString("ex_apply_id", wp.itemStr("ex_apply_id"));
    }
    if (empty(wp.itemStr("ex_emboss_reason")) == false) {
      wp.whereStr += " and  emboss_reason = :ex_emboss_reason ";
      setString("ex_emboss_reason", wp.itemStr("ex_emboss_reason"));
    }

    if (empty(wp.itemStr("ex_crt_user")) == false) {
      wp.whereStr += " and  crt_user = :ex_crt_user ";
      setString("ex_crt_user", wp.itemStr("ex_crt_user"));
    }
    return 1;
  }

  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " batchno " + " ,card_no " + " ,old_card_no " + " ,apply_id " + " ,corp_no "
        + " ,chi_name " + " ,eng_name " + " ,recno " + " ,emboss_reason " + " ,card_type "
        + " ,group_code " + " ,unit_code " + " ,mod_user " + " ,valid_fm " + " ,valid_to "
        + " ,emboss_source " + " ,remark_20 " // 20190822 add
        + " ,mail_type " // 20190822 add
        + " ,mail_branch " // 20190925 add
    ;


    wp.daoTable = " crd_emboss_tmp ";
    // wp.whereOrder = " order by card_no ";
    wp.whereOrder = " order by batchno,recno ";
    if (getWhereStr() != 1)
      return;
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdata();
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    // dddw_select();

  }

  @Override
  public void querySelect() throws Exception {

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    // String kk_old_card_no="";
    kk1Batchno = itemKk("data_k1");
    kk2Recno = itemKk("data_k2");
    if (empty(kk1Batchno)) {
      kk1Batchno = wp.itemStr("batchno");
    }
    if (empty(kk2Recno)) {
      kk2Recno = wp.itemStr("recno");
    }
    // kk_old_card_no = wp.item_ss("old_card_no");
    /*
     * if(empty(kk1_batchno)&&empty(kk2_recno)){ if(empty(kk_old_card_no)){ alert_err("請輸入舊卡號 !!!");
     * return; } if(!empty(kk_old_card_no)){ String
     * sql=" select move_status from crd_card_ext where card_no = :kk_old_card_no ";
     * setString("kk_old_card_no",kk_old_card_no); sqlSelect(sql);
     * if(sql_ss("move_status").equals("Y")){ alert_err("Error : 卡片正在 轉卡中 不可作業~ !!!"); return; } } }
     */

    wp.selectSQL = " hex(rowid) as rowid, mod_seqno " + " ,emboss_reason " + " ,recno "
        + " ,card_type " + " ,old_card_no "
        // + " ,old_card_no as kk_old_card_no"
        + " ,emboss_source " + " ,resend_note " + " ,to_nccc_code " + " ,apply_id "
        + " ,apply_id_code " + " ,birthday " + " ,chi_name " + " ,eng_name " + " ,corp_no "
        + " ,corp_no_code " + " ,valid_fm " + " ,valid_to " + " ,emboss_4th_data " + " ,crt_date "
        + " ,pm_id " + " ,pm_id_code " + " ,chg_addr_flag " + " ,member_id " + " ,fee_code "
        + " ,standard_fee " + " ,acct_type " + " ,acct_key " + " ,unit_code "
        // + " ,member_note "
        + " ,card_no " + " ,change_reason " + " ,status_code " + " ,reason_code "
        + " ,major_card_no " + " ,major_valid_fm " + " ,major_valid_to " + " ,group_code "
        + " ,source_code " + " ,mail_type " + " ,pvv " + " ,cvv " + " ,cvv2 " + " ,pvki "
        + " ,old_beg_date " + " ,force_flag " + " ,business_code " + " ,credit_lmt "
        + " ,fee_reason_code " + " ,annual_fee " + " ,cardno_code " + " ,apr_user " + " ,apr_date "
        + " ,fee_date " + " ,fee_error " + " ,emboss_date " + " ,nccc_batchno " + " ,nccc_recno "
        + " ,nccc_type " + " ,mod_user " + " ,to_char(mod_time,'YYYYMMDD') as mod_time "
        + " ,mod_pgm " + " ,voice_passwd " + " ,sup_flag " + " ,purchase_amt " + " ,reg_bank_no "
        + " ,risk_bank_no " + " ,act_no " + " ,vip " + " ,balance_amt " + " ,batchno "
        + " ,pin_block " + " ,old_end_date " + " ,decode(reason_code,'3','1','') as db_reason_code "
        + " ,ic_flag" + " ,branch " + " ,mail_attach1 " + " ,mail_attach2 " + " ,reissue_code "
        + " ,crt_user " + " ,curr_code " + " ,msisdn " + " ,service_type " + " ,se_id "
        + " ,mno_id " + " ,remark_20 " // 20190822 add
        + " ,electronic_code " // 20190916 add
        + " ,electronic_code as electronic_code_h " // 20190916 add
         + " ,electronic_code_old " //20190916 add
        // + " ,remark_20 "
        + " ,online_mark " + " ,mail_branch " // 20190925 add
    ;
    wp.daoTable = " crd_emboss_tmp ";
    wp.whereStr = " where 1=1 ";
    if (!empty(kk1Batchno)) {
      wp.whereStr += " and batchno = :batchno ";
      setString("batchno", kk1Batchno);
    }
    if (!empty(kk2Recno)) {
      wp.whereStr += " and recno = :recno ";
      setString("recno", kk2Recno);
    }
    /*
     * if(empty(kk1_batchno)&&empty(kk2_recno)){ if(!empty(kk_old_card_no)){ wp.whereStr +=
     * " and  old_card_no = :kk_old_card_no "; setString("kk_old_card_no",kk_old_card_no); } }
     */
    pageSelect();
    listWkdata2();
    listWkdata();

    if (wp.selectCnt <= 0) {
      wp.notFound = "N";
      /*
       * if(wf_cardno_data(is_cardno)!=1){ return; } is_audcode ="A";
       */
    } else {
      isAudcode = "U";
      wfGetReissuedata();
      // -- 從CRD_EMBOSS_TMP抓取,需重新檢核卡檔狀態碼
      if (wfChkCardStatus(wp.colStr("old_card_no")) != 1) {
        return;
      }
    }
    isCardno = wp.colStr("old_card_no");
    ofcRetrieve(wp.colStr("group_code"), wp.colStr("card_type"));
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Crdm0130Func(wp);

    if (ofValidation() != 1) {
      rc = -1;
      sqlCommit(rc);
      return;
    }
    // dddw_select();

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
    }
  }

  @Override
  public void dddwSelect() {

    try {
      if (wp.respHtml.indexOf("_detl") > 0) {

        wp.optionKey = wp.colStr("emboss_reason");
        this.dddwList("dddw_reissue_reason", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where 1=1 and wf_type = 'REISSUE_REASON'  order by wf_id");

        wp.optionKey = wp.colStr("mail_branch");
        this.dddwList("dddw_branch", "gen_brn", "branch", "brief_chi_name",
            "where 1=1  order by branch");
        if (!strAction.equals("S2")) {
          wp.optionKey = wp.colStr("reissue_code");
          StringBuffer str1 = new StringBuffer();
          str1.append("where 1=1 ");
          str1.append("and reissue_reason = '");
          str1.append(wp.colStr("emboss_reason"));
          str1.append("' order by reissue_code");
          dddwSelect2(str1.toString());
        }

        StringBuffer str2 = new StringBuffer();
        str2.append("where 1=1 ");
        str2.append("and group_code = '");
        str2.append(wp.colStr("group_code"));
        str2.append("' and card_type = '");
        str2.append(wp.colStr("card_type"));
        str2.append("' order by unit_code");
        wp.optionKey = wp.colStr("unit_code");
        this.dddwList("dddw_unit_code", "ptr_group_card_dtl", "unit_code", "", str2.toString());

      }
      wp.optionKey = wp.itemStr("ex_crt_user");
      this.dddwList("dddw_crt_user", "sec_user", "usr_id", "usr_cname",
          "where 1=1  order by usr_id");

    } catch (Exception ex) {
    }
  }

  void dddwSelect2(String lswhere) {

    try {
      this.dddwList("dddw_reissuecode", "ptr_reissue_code", "reissue_code",
          "'['||reissue_reason||','||reissue_code||']'||' '||content", lswhere);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  void listWkdata() {
    String wkData = "";
    for (int ll = 0; ll < wp.selectCnt; ll++) {
      wkData = wp.colStr(ll, "emboss_reason");
      wp.colSet(ll, "tt_emboss_reason", commString.decode(wkData, ",1,2,3", ",掛失,毀損,偽卡"));

      wkData = wp.colStr(ll, "emboss_source");
      wp.colSet(ll, "tt_emboss_source", commString.decode(wkData, ",1,2,3,4,5", ",新製卡,普昇金卡,整批續卡,提前續卡,重製"));

      wkData = wp.colStr(ll, "expire_chg_flag");
      wp.colSet(ll, "tt_expire_chg_flag", commString.decode(wkData, ",1,2,3", ",系統不續卡,預約不續卡,人工不續卡"));

      wkData = wp.colStr(ll, "sup_flag");
      wp.colSet(ll, "tt_sup_flag", commString.decode(wkData, ",0,1", ",正卡,附卡"));

      wkData = wp.colStr(ll, "reissue_reason");
      wp.colSet(ll, "tt_reissue_reason", commString.decode(wkData, ",1,2,3,4", ",掛失,毀損,偽卡,一般停卡"));

      wkData = wp.colStr(ll, "reissue_status");
      wp.colSet(ll, "tt_reissue_status", commString.decode(wkData, ",1,2,3,4", ",已登錄待製卡,製卡中,製卡完成,製卡失敗"));

      wkData = wp.colStr(ll, "mail_type");
      wp.colSet(ll, "db_mail_type", commString.decode(wkData, ",1,2,3,4,5,6,7,8,9",
          ",1.普掛,2.限掛,3.轉卡處發卡領取,4.分行,5.暫不寄,6.快捷,7.航空,8.快遞,9.其他"));
    }
  }

  void listWkdata2() {
    String serviceType = "";
    String sqlSelect = "";
    serviceType = wp.colStr("service_type");
    wp.colSet("tt_service_type", commString.decode(serviceType, ",01,02,03,04", ",UICC,Micro SD卡,嵌入式,外接式"));

    sqlSelect =
        "select wf_id,wf_id||'_'||wf_desc as tt_mno_id from ptr_sys_idtab where wf_id = :mno_id ";
    setString("mno_id", wp.colStr("mno_id"));
    sqlSelect(sqlSelect);
    wp.colSet("tt_mno_id", sqlStr("tt_mno_id"));
    wp.colSet("wf_id", sqlStr("wf_id"));
    selectCrdNcccCard1(wp.colStr("unit_code"), wp.colStr("card_type"));
  }

  void selectCrdNcccCard1(String unitCode, String cardType) {
    String sqlSelect =
        "select card_item||'_'||name as card_item_name from crd_nccc_card where card_item = :card_item";
    setString("card_item", unitCode + cardType);
    sqlSelect(sqlSelect);
    String cardItemName = sqlStr("card_item_name");
    wp.colSet("card_item_name", cardItemName);
  }

  void selectCrdNcccCard(String unitCode, String cardType) {
    String sqlSelect =
        "select card_item||'_'||name as card_item_name from crd_nccc_card where card_item = :card_item";
    setString("card_item", unitCode + cardType);
    sqlSelect(sqlSelect);
    String cardItemName = sqlStr("card_item_name");
    wp.colSet("card_item_name", cardItemName);
    // 20190916 add electronic_code
    sqlSelect = "select electronic_code " + "from crd_item_unit " + "where card_type =:card_type "
        + "and unit_code =:unit_code ";
    setString("card_type", cardType);
    setString("unit_code", unitCode);
    sqlSelect(sqlSelect);
    if (sqlRowNum > 0) {
      wp.colSet("electronic_code", sqlStr("electronic_code"));
      if (empty(wp.itemStr("electronic_code_old"))) {
        wp.colSet("electronic_code_old", sqlStr("electronic_code")); // 新增資料
      }
    }

  }

  int ofValidation() throws Exception {
    String lsCardMoldFlag = "", lsMobStatus = "", lsValidTo = "", lsCardType = "", lsGroupCode = "",
        lsExpireDate = "", lsCheckKeyExpire = "";
    String sMailType = "", sBranch = "";
    String lsCurrentCode = "", lsOppostDate = "", lsUnitDode = "";

    if (!strAction.equals("D")) {
      if (!check(wp.itemStr("eng_name"))) {
        alertErr("英文姓名只能輸入英數字及字符/.,'-");
        return -1;
      }
    }

    /*
     * if(!wp.item_ss("kk_old_card_no").equals(wp.item_ss("old_card_no"))){ alert_err("舊卡號不可變更");
     * return -1; }
     */
    if (!wp.itemStr("emboss_source").equals("5") && !empty(wp.itemStr("emboss_source"))) {
      alertErr("不可處理不是重製卡資料 !! ");
      return -1;
    }
    if (strAction.equals("D")) {
      if (!wp.itemStr("emboss_source").equals("5")) {
        alertErr("不可刪除不是重製卡資料");
        return -1;
      }
    }
    if (!strAction.equals("D")) {
      lsCardMoldFlag = wp.itemStr("db_card_mold_flag");
      if (lsCardMoldFlag.equals("M")) {
        lsMobStatus = wp.itemStr("db_mob_status");
        if (wp.itemStr("emboss_reason").equals("2") && !lsMobStatus.equals("03")) {
          alertErr("手機信用卡僅允許未下載，且逾期30日者毀損重製");
          return -1;
        }
      }
    }
    // 20191219 add new card_no check
    if (strAction.equals("A")) {
      if (!empty(wp.itemStr("ex_card_no"))) {
        String lsOldCardNo = wp.itemStr("old_card_no");
        String lsCardNo = wp.itemStr("ex_card_no");
        if (!strMid(lsOldCardNo, 0, 6).equals(strMid(lsCardNo, 0, 6))) {
          alertErr("新舊卡號前6碼不同!!");
          return -1;
        }
      }
    }
    //

    lsValidTo = wp.itemStr("valid_to");
    lsCardType = wp.itemStr("card_type");
    lsGroupCode = wp.itemStr("group_code");
    lsUnitDode = wp.itemStr("unit_code");

//    if (wp.itemStr("ic_flag").equals("Y")) {
//
//
//      // ptr_iccard 改 crd_item_unit
//      String sql1 = "select b.expire_date ls_expire_date,a.check_key_expire ls_check_key_expire "
//          + "from crd_item_unit as a , ptr_ickey as b where a.card_type  = :ls_card_type "
//          + "and (a.unit_code = :ls_group_code or a.unit_code ='') "
//          + "and b.key_type = UF_BIN_TYPE( :old_card_no ) " + "and b.key_id = a.key_id ";
//      setString("old_card_no", wp.itemStr("old_card_no"));
//      setString("ls_card_type", lsCardType);
//      setString("ls_group_code", lsGroupCode);
//      sqlSelect(sql1);
//      lsExpireDate = sqlStr("ls_expire_date");
//      lsCheckKeyExpire = sqlStr("ls_check_key_expire");
//      if (sqlRowNum <= 0) {
//        alertErr("ptr_ickey error -> " + "card_type=" + lsCardType + ",unit_code=" + lsGroupCode);
//        return -1;
//      }
//      if (lsCheckKeyExpire.equals("Y") && this.toInt(lsValidTo) > this.toInt(lsExpireDate)) {
//        alertErr("Error: 新效期超過晶片卡效期  ! -> " + lsValidTo + "," + lsExpireDate);
//        return -1;
//      }
//    }
    if (empty(wp.itemStr("reissue_code").trim()) && !strAction.equals("D")) {
      alertErr("需輸入重製說明");
      return -1;
    }
    // --JH(93038)寄件別--------------------------------
    sMailType = wp.itemStr("mail_type");
    sBranch = wp.itemStr("mail_branch");
    if (sMailType.equals("4") && empty(sBranch)) {
      alertErr("寄件別為分行, 請輸入分行代碼");
      return -1;
    } else if (sMailType.equals("3")) {
      // 20191003 user提取消限制
      // if(!s_branch.equals("990")){
      // alert_err("寄件別是自取, 分行代碼需為990");
      // return -1;
      // //func.vars_set("branch","990"); //20190923 update:error to rteutrn
      // }
    } else if ((!sMailType.equals("3") && !sMailType.equals("4")) && !empty(sBranch)) {
      alertErr("寄件別不是分行, 分行代碼需為空白");
      return -1;
    }
    if (!empty(sBranch)) {
      String sql2 = "select count(*) as l_cnt from gen_brn where branch = :s_branch ";
      setString("s_branch", sBranch);
      sqlSelect(sql2);
      if (sqlNum("l_cnt") == 0) {
        alertErr("分行代碼 輸入錯誤");
        return -1;
      }
    }

    // 指定卡號功能，該卡號系統需檢核不能重覆發卡
    if (!wp.itemStr("emboss_reason").equals("2") && !empty(wp.itemStr("ex_card_no"))) {
      String lsBinNo = strMid(wp.itemStr("ex_card_no"), 0, 6);
      String lsSeqno = strMid(wp.itemStr("ex_card_no"), 6, wp.itemStr("ex_card_no").length());

      // 20200206新增判斷 start Ru
      String sqlSelect = "select bin_no, beg_seqno, end_seqno " + "from crd_cardno_range "
          + "where group_code = :group_code and card_type = :card_type and bin_no = :bin_no and card_flag = '1' and post_flag = 'Y' ";
      setString("group_code", wp.itemStr("group_code"));
      setString("card_type", wp.itemStr("card_type"));
      setString("bin_no", wp.itemStr("bin_no"));
      sqlSelect(sqlSelect);
      String begSeqno = sqlStr("beg_seqno");
      String endSeqno = sqlStr("end_seqno");
      if (sqlRowNum == 0 || (lsSeqno.substring(0, 9).compareTo(begSeqno) < 0
          || lsSeqno.substring(0, 9).compareTo(endSeqno) > 0)) {
        alertErr("該指定卡號不在區間內");
        return -1;
      }

      sqlSelect = "select * from crd_prohibit where card_no = :card_no ";
      setString("card_no", wp.itemStr("ex_card_no"));
      sqlSelect(sqlSelect);
      if (sqlRowNum > 0) {
        alertErr("該指定卡號為禁號");
        return -1;
      }

      busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
      String tmpX15 = strMid(wp.itemStr("ex_card_no"), 0, 15);
      String tmpX16 = strMid(wp.itemStr("ex_card_no"), 15, 16);
      String hSChkDif = comm.cardChkCode(tmpX15);
      if (!hSChkDif.equals(tmpX16)) {
        alertErr("該指定卡號檢查碼有誤");
        return -1;
      }
      // 20200206新增判斷 end Ru

      sqlSelect =
          "select reserve,use_date from crd_seqno_log where bin_no = :ls_bin_no and seqno = :ls_seqno ";
      setString("ls_bin_no", lsBinNo);
      setString("ls_seqno", lsSeqno);
      sqlSelect(sqlSelect);
      if (sqlStr("reserve").equals("Y") && !empty(sqlStr("use_date"))) {
        alertErr("此號碼已使用 !!");
        return -1;
      }

      // 20200206新增判斷 start Ru
      // else{
      // if(sql_nrow<=0){
      // alert_err("卡號不存在");
      // return -1;
      // }
      if (sqlRowNum > 0) {
        busi.SqlPrepare sp = new SqlPrepare();
        sp.sql2Update("crd_seqno_log");
        sp.ppstr("reserve", "Y");
        sp.ppstr("card_item", wp.itemStr("unit_code") + wp.itemStr("card_type"));
        sp.ppstr("unit_code", lsUnitDode);
        sp.ppstr("use_id", wp.loginUser);
        sp.ppstr("use_date", getSysDate());
        sp.ppstr("mod_user", wp.loginUser);
        sp.ppstr("mod_pgm", wp.modPgm());
        sp.addsql(", mod_time = sysdate ");
        sp.addsql(", mod_seqno = nvl(mod_seqno, 0) + 1", "");
        sp.sql2Where(" where bin_no=?", lsBinNo);
        sp.sql2Where(" and seqno=?", lsSeqno);
        sqlExec(sp.sqlStmt(), sp.sqlParm());

        if (sqlRowNum <= 0) {
          alertErr("update crd_seqno_log err");
          return -1;
        }
      } else {
        busi.SqlPrepare sp = new SqlPrepare();
        sp.sql2Insert("crd_seqno_log");
        sp.ppstr("card_type_sort", "0");
        sp.ppstr("bin_no", lsBinNo);
        sp.ppstr("seqno", lsSeqno);
        sp.ppstr("card_type", lsCardType);
        sp.ppstr("group_code", lsGroupCode);
        sp.ppstr("card_flag", "1");
        sp.ppstr("reserve", "Y");
        sp.ppstr("trans_no", "");
        sp.ppstr("use_date", getSysDate());
        sp.ppstr("use_id", wp.loginUser);
        sp.ppstr("crt_date", getSysDate());
        sp.ppstr("card_item", wp.itemStr("unit_code") + wp.itemStr("card_type"));
        sp.ppstr("unit_code", lsUnitDode);
        sp.ppstr("seqno_old", lsSeqno.substring(0, 9));
        sp.ppstr("mod_user", wp.loginUser);
        sp.addsql(", mod_time ", ", sysdate ");
        sp.ppstr("mod_pgm", wp.modPgm());
        sqlExec(sp.sqlStmt(), sp.sqlParm());

        if (sqlRowNum <= 0) {
          alertErr("insert crd_seqno_log err");
          return -1;
        }
      }
      // 20200206新增判斷 end Ru
    }

    if (strAction.equals("D")) {
      if (wfUpdReissue() != 1) {
        sqlCommit(0);
        return -1;
      } else {
        sqlCommit(1);
        return 1;
      }
    }
    // --check old_cardno--
    lsCurrentCode = wp.itemStr("current_code");
    lsOppostDate = wp.itemStr("oppost_date");



    if (wp.itemStr("online_mark").equals("Y")) {
      func.varsSet("to_nccc_code", "N");
      func.varsSet("reason_code", "3");
    } else {
      func.varsSet("to_nccc_code", "Y");
      func.varsSet("reason_code", "");
    }

    if (wfChkOppost_date(wp.itemStr("emboss_reason"), lsCurrentCode, lsOppostDate) != 1) {
      alertMsg("不可新增,修改此資料");
      return -1;
    }

    if (this.toInt(wp.itemStr("valid_to")) < this.toInt(wp.itemStr("valid_fm"))) {
      alertErr("有效期迄需大於起日");
      return -1;
    }

    if (strAction.equals("A")) {

      if (wp.itemStr("current_code").equals("0")) {
        if (wp.itemStr("reissue_status").equals("1")) {
          alertErr("此卡片已在重製卡中");
          return -1;
        }
        if (wp.itemStr("reissue_status").equals("2")) {
          alertErr("此卡片已在送製卡中,不可再做重製卡");
          return -1;
        }
      }
      // -- modified by shu 20020925
      if (wp.itemStr("emboss_reason").equals("1") || wp.itemStr("emboss_reason").equals("3")) {
        if (!empty(wp.itemStr("db_new_card_no"))) {
          alertErr("此卡片已重製成功過,不可重複重製");
          return -1;
        }
      }
      if (wfMoveEmbossTmp() != 1) {
        sqlCommit(0);
        return -1;
      }
    }
    if (wfUpdReissue() != 1) {
      sqlCommit(0);
      alertMsg("寫入失敗");
      return -1;
    }
    return 1;
  }

  int wfUpdReissue() {
    String lsCardno = "";
    String lsDate = "", lsReason = "", lsReissueDate = "", lsReissueReason = "",
        lsReissueStatus = "";
    int liExist = 0;
    lsDate = getSysDate();
    lsCardno = wp.itemStr("old_card_no");
    lsReason = wp.itemStr("emboss_reason");
    String sqlSelect =
        "select reissue_date,reissue_reason,reissue_status from crd_reissue_tmp where card_no = :ls_cardno ";
    setString("ls_cardno", lsCardno);
    sqlSelect(sqlSelect);
    String reissueDate = sqlStr("reissue_date");
    String reissueReason = sqlStr("reissue_reason");
    String reissueStatus = sqlStr("reissue_status");
    if (sqlRowNum > 0) {
      liExist = 1;
    }
    busi.SqlPrepare sp = new SqlPrepare();
    if (strAction.equals("A") || strAction.equals("U")) {
      sp.sql2Update("crd_card");
      sp.ppstr("reissue_reason", lsReason);
      sp.ppstr("reissue_status", "1");
      sp.ppstr("reissue_date", lsDate);
      sp.sql2Where(" where card_no=?", lsCardno);
      sqlExec(sp.sqlStmt(), sp.sqlParm());
      if (sqlRowNum <= 0) {
        alertErr("寫入卡片重製日期失敗");
        return -1;
      }

      if (strAction.equals("A")) {
        if (liExist == 0) {
          sp.sql2Insert("crd_reissue_tmp");
          sp.ppstr("card_no", lsCardno);
          sp.ppstr("reissue_date", wp.itemStr("is_old_reissue_date"));
          sp.ppstr("reissue_reason", wp.itemStr("is_old_reissue_reason"));
          sp.ppstr("reissue_status", wp.itemStr("is_old_reissue_status"));
          sp.ppstr("crt_date", getSysDate());
          sp.ppstr("crt_user", wp.loginUser);
          sp.ppstr("mod_user", wp.loginUser);
          sp.ppstr("mod_pgm", wp.modPgm());
          sp.ppnum("mod_seqno", 1);
          sp.addsql(", mod_time ", ", sysdate ");
          sqlExec(sp.sqlStmt(), sp.sqlParm());
        } else {
          sp.sql2Update("crd_reissue_tmp");
          sp.ppstr("reissue_date", wp.itemStr("is_old_reissue_date"));
          sp.ppstr("reissue_reason", wp.itemStr("is_old_reissue_reason"));
          sp.ppstr("reissue_status", wp.itemStr("is_old_reissue_status"));
          sp.ppstr("mod_user", wp.loginUser);
          sp.ppstr("mod_pgm", wp.modPgm());
          sp.addsql(", mod_time = sysdate ");
          sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1");
          sp.sql2Where(" where card_no=?", lsCardno);
          sqlExec(sp.sqlStmt(), sp.sqlParm());
        }
        if (sqlRowNum <= 0) {
          alertErr("寫入crd_reissue_tmp錯誤");
          return -1;
        }
      }
    }
    if (strAction.equals("D")) {
      lsReissueDate = "";
      lsReissueReason = "";
      lsReissueStatus = "";
      if (liExist == 1) {
        lsReissueDate = reissueDate;
        lsReissueReason = reissueReason;
        lsReissueStatus = reissueStatus;
      }
      sp.sql2Update("crd_card");
      sp.ppstr("reissue_reason", lsReissueReason);
      sp.ppstr("reissue_status", lsReissueStatus);
      sp.ppstr("reissue_date", lsReissueDate);
      sp.ppstr("mod_user", wp.loginUser);
      sp.ppstr("mod_pgm", wp.modPgm());
      sp.addsql(", mod_time = sysdate ");
      sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1");
      sp.sql2Where(" where card_no=?", lsCardno);
      sqlExec(sp.sqlStmt(), sp.sqlParm());
      if (sqlRowNum <= 0) {
        alertErr("寫入卡片重製日期失敗");
        return -1;
      }

      if (liExist == 1) {
        String sqlDelete = "delete crd_reissue_tmp where card_no = :ls_cardno";
        setString("ls_cardno", lsCardno);
        sqlExec(sqlDelete);
        if (sqlRowNum <= 0) {
          alertErr("刪除crd_reissue_tmp ERROR");
          return -1;
        }
      }
      // 20191003 user提出刪除crd_reissue_tmp需還原已取號資料
      String lsBinNo = strMid(wp.itemStr("card_no"), 0, 6);
      String lsSeqno = strMid(wp.itemStr("card_no"), 6, wp.itemStr("card_no").length());
      if (!empty(wp.itemStr("reserve_date"))) {
        sp.sql2Update("crd_seqno_log");
        sp.ppstr("reserve", "Y");
        sp.ppstr("use_id", "");
        sp.ppstr("use_date", "");
        sp.ppstr("mod_user", wp.loginUser);
        sp.ppstr("mod_pgm", wp.modPgm());
        sp.addsql(", mod_time = sysdate ");
        sp.addsql(", mod_seqno = mod_seqno+1");
        sp.sql2Where(" where bin_no=?", lsBinNo);
        sp.sql2Where(" and seqno=?", lsSeqno);
        sqlExec(sp.sqlStmt(), sp.sqlParm());

        if (sqlRowNum <= 0) {
          alertErr("update crd_seqno_log err");
          return -1;
        }
      } else {
        String sqlDelete = "delete crd_seqno_log where bin_no = :ls_bin_no and seqno = :ls_seqno ";
        setString("ls_bin_no", lsBinNo);
        setString("ls_seqno", lsSeqno);
        sqlExec(sqlDelete);
      }
    }

    return 1;
  }

  int wfChkOppost_date(String asEmbossReason, String asCurrentCode, String asOppostDate)
      throws ParseException {
    if (asEmbossReason.equals("2")) {
      if (!asCurrentCode.equals("0") || !empty(asOppostDate)) {
        wp.alertMesg += "<script language='javascript'> alert('停卡不可登錄毀損重製')</script>";
        return -1;
      }
    } else {
      if (asCurrentCode.equals("0") || empty(asOppostDate)) {
        if (asEmbossReason.equals("1")) {
          wp.alertMesg += "<script language='javascript'> alert('活卡不可登錄掛失重製')</script>";
        }
        if (asEmbossReason.equals("3")) {
          wp.alertMesg += "<script language='javascript'> alert('活卡不可登錄偽卡重製')</script>";
        }
        return -1;
      }
    }

    // 掛失和偽卡重製日期需在6個月內
    String lsSysdate = "", lsChk = "";
    if (asCurrentCode.equals("1") || asCurrentCode.equals("2") || asCurrentCode.equals("5")) {
      lsSysdate = getSysDate();
      lsChk = ofRelativeMm(asOppostDate, 0, 6);
      if (this.toNum(lsChk) < this.toNum(lsSysdate)) {
        wp.alertMesg += "<script language='javascript'> alert('停卡已超過6個月不可重製')</script>";
        return -1;
      }
    }

    return 1;
  }


  int wfMoveEmbossTmp() {
    String lsCardno = "", lsBatchno1 = "", lsBatchno = "", lsCreateDate = "";
    String lsActForceFlag = "";

    String liRecno = "0";
    String[] lsCrdValue = new String[36];
    String[] lsOIdnoValue = new String[5];
    lsCardno = wp.itemStr("old_card_no");
    String sql1 = "select count(*) as li_count from crd_emboss_tmp where old_card_no = :ls_cardno";
    setString("ls_cardno", lsCardno);
    sqlSelect(sql1);
    if (sqlNum("li_count") > 0) {
      alertErr("此資料在待製卡中");
      return -1;
    }
    if (wp.itemStr("reissue_status").equals("1") || wp.itemStr("reissue_status").equals("2")) {
      alertErr("此資料在製卡狀態中");
      return -1;
    }
    lsCreateDate = getSysDate();
    // --Get Batchno--
    lsBatchno1 = wp.itemStr("batchno");
    if (empty(lsBatchno1)) {
      lsBatchno1 = strMid(lsCreateDate, 2, 6);
    }
    if (empty(lsBatchno1)) {
      return -1;
    }

    String sql2 =
        "select max(batchno) as ls_batchno from crd_emboss_tmp where substr(batchno,1,6) = :ls_batchno1";
    setString("ls_batchno1", lsBatchno1);
    sqlSelect(sql2);
    lsBatchno = sqlStr("ls_batchno");
    if (empty(lsBatchno)) {
      lsBatchno = lsBatchno1 + "01";
    } else {
      String sql3 =
          "select max(recno)+1 as li_recno from crd_emboss_tmp where batchno = :ls_batchno";
      setString("ls_batchno", lsBatchno);
      sqlSelect(sql3);
      liRecno = sqlStr("li_recno");
    }

    if (empty(liRecno) || liRecno.equals("0")) {
      liRecno = "1";
    }
    String sql3 = "select card_type" + ",unit_code " + ",member_note "
        + ",UF_IDNO_ID(id_p_seqno) as id_no " + ",UF_IDNO_ID(major_id_p_seqno) as major_id"
        + ",major_card_no" + ",current_code" + ",group_code" + ",source_code" + ",corp_no"
        + ",corp_no_code" + ",eng_name" + ",new_beg_date" + ",new_end_date" + ",acct_type"
        + ",substr(UF_ACNO_KEY(acno_p_seqno),1,11) as acct_key" + ",id_p_seqno" + ",acno_p_seqno"
        + ",pvv" + ",cvv" + ",cvv2" + ",pvki" + ",reg_bank_no" + ",force_flag" + ",emboss_data"
        + ",sup_flag" + ",ic_flag" + ",reissue_date" + ",reissue_reason" + ",reissue_status"
        + ",curr_code " + ",branch " + " from crd_card " + " where card_no = :ls_cardno ";
    setString("ls_cardno", lsCardno);
    sqlSelect(sql3);
    lsCrdValue[1] = sqlStr("card_type");
    lsCrdValue[2] = sqlStr("unit_code");
    lsCrdValue[3] = sqlStr("member_note");
    lsCrdValue[4] = sqlStr("id_no");
    lsCrdValue[6] = sqlStr("major_id");
    lsCrdValue[8] = sqlStr("major_card_no");
    lsCrdValue[9] = sqlStr("current_code");
    lsCrdValue[10] = sqlStr("group_code");
    lsCrdValue[11] = sqlStr("source_code");
    lsCrdValue[12] = sqlStr("corp_no");
    lsCrdValue[13] = sqlStr("corp_no_code");
    lsCrdValue[14] = sqlStr("eng_name");
    lsCrdValue[15] = sqlStr("new_beg_date");
    lsCrdValue[16] = sqlStr("new_end_date");
    lsCrdValue[17] = sqlStr("acct_type");
    lsCrdValue[18] = sqlStr("acct_key");
    lsCrdValue[19] = sqlStr("id_p_seqno");
    lsCrdValue[20] = sqlStr("acno_p_seqno");
    lsCrdValue[21] = sqlStr("pvv");
    lsCrdValue[22] = sqlStr("cvv");
    lsCrdValue[23] = sqlStr("cvv2");
    lsCrdValue[24] = sqlStr("pvki");
    lsCrdValue[25] = sqlStr("reg_bank_no");
    lsCrdValue[26] = sqlStr("force_flag");
    lsCrdValue[27] = sqlStr("emboss_data");
    lsCrdValue[28] = sqlStr("sup_flag");
    lsCrdValue[29] = sqlStr("ic_flag");
    lsCrdValue[34] = sqlStr("curr_code");
    lsCrdValue[35] = sqlStr("branch");
    if (sqlRowNum <= 0) {
      alertErr("抓取卡檔失敗");
      return -1;
    }
    String sqlSelectIdNoCode =
        "select b.id_no_code from crd_card as a join crd_idno as b on a.id_p_seqno = b.id_p_seqno where card_no = :ls_cardno ";
    setString("ls_cardno", lsCardno);
    sqlSelect(sqlSelectIdNoCode);
    lsCrdValue[5] = sqlStr("id_no_code");

    String sqlSelectMajorIdDode =
        "select b.id_no_code as major_id_code from crd_card as a join crd_idno as b on a.major_id_p_seqno = b.id_p_seqno where card_no = :ls_cardno ";
    setString("ls_cardno", lsCardno);
    sqlSelect(sqlSelectMajorIdDode);
    lsCrdValue[7] = sqlStr("major_id_code");

    if (lsCrdValue[28].equals("1")) {
      String sql4 =
          "select new_beg_date,new_end_date,id_p_seqno from crd_card where card_no = :ls_crd_value";
      setString("ls_crd_value", lsCrdValue[8]);
      sqlSelect(sql4);
      lsCrdValue[31] = sqlStr("new_beg_date");
      lsCrdValue[32] = sqlStr("new_end_date");
      lsCrdValue[33] = sqlStr("id_p_seqno");
      if (sqlRowNum <= 0) {
        alertErr("抓取不到正卡資料");
        return -1;
      }
    }
    String sql5 =
        "select chi_name, birthday,voice_passwd,sex  from crd_idno where id_p_seqno = :ls_crd_value";
    setString("ls_crd_value", lsCrdValue[19]);
    sqlSelect(sql5);
    lsOIdnoValue[1] = sqlStr("chi_name");
    lsOIdnoValue[2] = sqlStr("birthday");
    lsOIdnoValue[3] = sqlStr("voice_passwd");
    lsOIdnoValue[4] = sqlStr("sex");
    if (sqlRowNum <= 0) {
      alertErr("抓取卡人檔失敗");
      return -1;
    }
    String sql6 =
        "select line_of_credit_amt,chg_addr_date,risk_bank_no from act_acno  where acno_p_seqno = :ls_crd_value";
    setString("ls_crd_value", lsCrdValue[20]);
    sqlSelect(sql6);
    String liActCreditAmt = sqlStr("line_of_credit_amt");
    String lsChgAddrDate = sqlStr("chg_addr_date");
    String lsRiskBankNo = sqlStr("risk_bank_no");
    if (sqlRowNum <= 0) {
      alertErr("select act_acno err");
      return -1;
    }
    func.varsSet("batchno", lsBatchno);
    func.varsSet("recno", liRecno + "");
    func.varsSet("emboss_source", "5");// 重製----
    func.varsSet("emboss_reason", wp.itemStr("emboss_reason"));
    func.varsSet("to_nccc_code", "Y");
    func.varsSet("nccc_type", "1");// -- 用新製卡格式送NCCC
    func.varsSet("card_type", lsCrdValue[1]);
    func.varsSet("unit_code", wp.itemStr("unit_code"));
    func.varsSet("member_note", lsCrdValue[3]);
    func.varsSet("old_card_no", wp.itemStr("old_card_no"));
    func.varsSet("ic_flag", wp.itemStr("ic_flag"));// -- modified by shu 2002/11/15
    // -- 毀損重製,用舊卡號,CVV,PVV,CVV2,PVKI
    if (wp.itemStr("emboss_reason").equals("2")) {
      func.varsSet("card_no", wp.itemStr("old_card_no"));
      func.varsSet("emboss_4th_data", wp.itemStr("emboss_4th_data"));
      func.varsSet("voice_passwd", lsOIdnoValue[3]);
    }
    func.varsSet("status_code", "1");
    if (wp.itemStr("online_mark").equals("Y")) {
      func.varsSet("to_nccc_code", "N");
      func.varsSet("reason_code", "3");
    }
    func.varsSet("apply_id", lsCrdValue[4]);
    func.varsSet("apply_id_code", lsCrdValue[5]);
    // -- 正附卡
    func.varsSet("sup_flag", lsCrdValue[28]);
    // -- 正卡資料
    func.varsSet("pm_id", lsCrdValue[6]);
    func.varsSet("pm_id_code", lsCrdValue[7]);
    func.varsSet("ls_crd_value28", lsCrdValue[28]);
    if (lsCrdValue[28].equals("1")) {
      func.varsSet("major_card_no", lsCrdValue[8]);
      func.varsSet("major_valid_fm", lsCrdValue[31]);
      func.varsSet("major_valid_to", lsCrdValue[32]);
    }
    func.varsSet("group_code", lsCrdValue[10]);
    func.varsSet("source_code", lsCrdValue[11]);
    func.varsSet("corp_no", lsCrdValue[12]);
    func.varsSet("corp_no_code", lsCrdValue[13]);
    func.varsSet("acct_type", lsCrdValue[17]);
    func.varsSet("acct_key", lsCrdValue[18]);
    func.varsSet("chi_name", lsOIdnoValue[1]);
    func.varsSet("eng_name", wp.itemStr("eng_name"));
    func.varsSet("birthday", lsOIdnoValue[2]);
    // func.vars_set("force_flag", ls_act_force_flag);
    func.varsSet("credit_lmt", liActCreditAmt);
    func.varsSet("old_beg_date", lsCrdValue[15]);
    func.varsSet("old_end_date", lsCrdValue[16]);
    func.varsSet("reg_bank_no", lsCrdValue[25]);
    func.varsSet("risk_bank_no", lsRiskBankNo);
    func.varsSet("force_flag", lsCrdValue[26]);
    func.varsSet("crt_date", lsCreateDate);
    func.varsSet("crt_user", wp.loginUser);
    func.varsSet("curr_code", lsCrdValue[34]);
    func.varsSet("branch", lsCrdValue[35]);
    return 1;
  }

  int wfGetSpecFlag(String asGroupCode, String asCardType) {
    if (empty(asGroupCode) || asGroupCode.equals("0000")) {
      return 1;
    }
    String lsGroupCode = asGroupCode;
    String lsCardType = asCardType;
    String sqlSelect =
        "select spec_flag from ptr_group_card  where group_code = :ls_group_code and card_type = :ls_card_type";
    setString("ls_group_code", lsGroupCode);
    setString("ls_card_type", lsCardType);
    sqlSelect(sqlSelect);
    if (sqlRowNum <= 0) {
      wp.alertMesg += "<script language='javascript'> alert('卡樣卡種抓取不到')</script>";
      return -1;
    }
    wp.colSet("spec_flag", sqlStr("spec_flag"));

    return 1;
  }

  int wfChkExpireDate(String asBegDate, String asEndDate, String cardType, String groupCode,
      String unitCode) throws ParseException {
    // -- 檢核六個月以內,自動展期,到參數抓取展期年
    // -- 六個月以上照原來new_end_date
    String lsDate1 = "", lsDate2 = "";
    String lsVal1 = asBegDate;
    String lsVal2 = asEndDate;
    String lsVal3 = cardType;
    String lsVal4 = groupCode;
    String sqlSelect =
        "select extn_year from crd_item_unit where card_type = :ls_val3 and unit_code = :ls_val4";
    setString("ls_val3", lsVal3);
    setString("ls_val4", lsVal4);
    sqlSelect(sqlSelect);
    int liExtnYear = this.toInt(sqlStr("extn_year"));
    // 2,0,0--年月日,6個月內自動展期,否則照舊
    String lsChk = ofRelativeMm(getSysDate(), 0, 6);
    // -- 變更效期

    if (this.toInt(strMid(lsVal2, 0, 6)) <= this.toInt(strMid(lsChk, 0, 6))) {
      lsDate1 = strMid(getSysDate(), 0, 6) + "01";
      lsDate2 = ofRelativeMm(lsVal2, liExtnYear, 0);
    } else {
      if (wp.colStr("emboss_reason").equals("2")) {
        // EMBOSS_REASON=2，原效期為六個月以上
        setString("cardItem", unitCode + cardType);
        sqlSelect = "select REISSUE_EXTN_MM from CRD_ITEM_UNIT where CARD_ITEM = :cardItem";
        sqlSelect(sqlSelect);
        lsDate1 = strMid(getSysDate(), 0, 6) + "01";
        lsDate2 = ofRelativeMm(lsVal2, 0, sqlInt("reissue_extn_mm"));
      } else {
        lsDate1 = strMid(getSysDate(), 0, 6) + "01";
        lsDate2 = lsVal2;
      }
    }
    wp.colSet("valid_fm", lsDate1);
    wp.colSet("valid_to", lsDate2);
    int liSystemDd = this.toInt(strMid(getSysDate(), 6, 2));
    if (liSystemDd >= 25) {
      sqlSelect =
          "select to_char(add_months(to_date( :ls_date1 ,'yyyymmdd'),1),'yyyymm')||'01' as ls_date1 from dual ";
      setString("ls_date1", lsDate1);
      sqlSelect(sqlSelect);
      lsDate1 = sqlStr("ls_date1");
      if (sqlRowNum <= 0) {
        wp.alertMesg += "<script language='javascript'> alert('日期資料轉換錯誤 !')</script>";
        return -1;
      }
    }
    wp.colSet("valid_fm", lsDate1);
    return 1;
  }


  String ofRelativeMm(String ymd, int y, int m) throws ParseException {
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    Date date = format.parse(ymd);
    cal.setTime(date);
    cal.add(Calendar.YEAR, y);
    cal.add(Calendar.MARCH, m);
    String lsChk = format.format(cal.getTime());
    return lsChk;
  }

  int wfAardnoAata(String alData) throws ParseException {
    String lsSysdate = getSysDate();
    String lsKey1 = alData;
    String sqlSelect = "select card_type " + ",UF_IDNO_ID(id_p_seqno) as id_no " + ",0 as id_code "
        + ",new_beg_date " + ",new_end_date " + ",id_p_seqno " + ",corp_no " + ",eng_name "
        + ",group_code " + ",unit_code " + ",reissue_date " + ",reissue_reason "
        + ",reissue_status " + ",current_code " + ",oppost_date " + ",reg_bank_no " + ",force_flag "
        + ",emboss_data " + ",change_status " + ",expire_chg_flag " + ",sup_flag "
        + ",major_card_no " + ",UF_IDNO_ID(major_id_p_seqno) as major_id " + ",0 as major_id_code "
        + ",new_card_no " + ",ic_flag "
        // + ",block_reason "
        + ",acno_p_seqno " + ",bin_no " + ",electronic_code "
        // + ",substr(block_reason2,1,2) "
        // + ",substr(block_reason2,3,2) "
        // + ",substr(block_reason2,5,2) "
        // + ",substr(block_reason2,7,2)
        + "from crd_card " + "where card_no = :ls_key1 ";
    setString("ls_key1", lsKey1);
    sqlSelect(sqlSelect);
    String lsCardType = sqlStr("card_type");
    String lsApplyId = sqlStr("id_no");
    String lsApplyIdCode = sqlStr("id_no_code");
    String lsNewBegDate = sqlStr("new_beg_date");
    String lsNewEndDate = sqlStr("new_end_date");
    String lsIdPSeqno = sqlStr("id_p_seqno");
    String lsCorpNo = sqlStr("corp_no");
    String lsEngName = sqlStr("eng_name");
    String lsGroupCode = sqlStr("group_code");
    String lsUnitCode = sqlStr("unit_code");
    String lsReissue1 = sqlStr("reissue_date");
    String lsReissue2 = sqlStr("reissue_reason");
    String lsReissue3 = sqlStr("reissue_status");
    String lsCurrentCode = sqlStr("current_code");
    String lsOppostDate = sqlStr("oppost_date");
    String lsRegBankNo = sqlStr("reg_bank_no");
    String lsForceFlag = sqlStr("force_flag");
    String lsEmbossData = sqlStr("emboss_data");
    String lsChangeStatus = sqlStr("change_status");
    String lsExpireChgFlag = sqlStr("expire_chg_flag");
    String lsSupFlag = sqlStr("sup_flag");
    String lsMajorCardNo = sqlStr("major_card_no");
    String lsMajorId = sqlStr("major_id");
    String lsMajorIdCode = sqlStr("major_id_code");
    String lsNewCardNo = sqlStr("new_card_no");
    String lsIcFlag = sqlStr("ic_flag");
    String lsAcnoPSeqno = sqlStr("acno_p_seqno");
    String lsBinNo = sqlStr("bin_no");

    wp.colSet("old_card_no", alData);
    wp.colSet("electronic_code", sqlStr("electronic_code"));
    wp.colSet("electronic_code_old", sqlStr("electronic_code"));
    if (sqlRowNum <= 0) {
      wp.alertMesg += "<script language='javascript'> alert('此卡號不存在於卡檔中')</script>";
      return -1;
    }

    sqlSelect = "select block_reason1" + ",block_reason2" + ",block_reason3" + ",block_reason4"
        + ",block_reason5" + " from cca_card_acct "
        + " where acno_p_seqno = :acno_p_seqno and debit_flag = 'N' ";
    setString("acno_p_seqno", lsAcnoPSeqno);
    sqlSelect(sqlSelect);
    String lsBlockReason = sqlStr("block_reason1");
    String lsBlockReason2 = sqlStr("block_reason2");
    String lsBlockReason3 = sqlStr("block_reason3");
    String lsBlockReason4 = sqlStr("block_reason4");
    String lsBlockReason5 = sqlStr("block_reason5");
    if (!empty(lsBlockReason)) {
      wp.alertMesg += "<script language='javascript'> alert('凍結原因：" + lsBlockReason + "')</script>";
      return -1;
    }
    if (!empty(lsBlockReason2)) {
      wp.alertMesg +=
          "<script language='javascript'> alert('凍結原因：" + lsBlockReason2 + "')</script>";
      return -1;
    }
    if (!empty(lsBlockReason3)) {
      wp.alertMesg +=
          "<script language='javascript'> alert('凍結原因：" + lsBlockReason3 + "')</script>";
      return -1;
    }
    if (!empty(lsBlockReason4)) {
      wp.alertMesg +=
          "<script language='javascript'> alert('凍結原因：" + lsBlockReason4 + "')</script>";
      return -1;
    }
    if (!empty(lsBlockReason5)) {
      wp.alertMesg +=
          "<script language='javascript'> alert('凍結原因：" + lsBlockReason5 + "')</script>";
      return -1;
    }
    selectCrdNcccCard(lsUnitCode, lsCardType);
    sqlSelect = "select payment_rate1 from act_acno "
        + "  where decode(payment_rate1,'','00',payment_rate1) not in ('00','0A','0B','0C','0D','0E') "
        + " and acno_p_seqno = :acno_p_seqno ";
    setString("acno_p_seqno", lsAcnoPSeqno);
    sqlSelect(sqlSelect);
    String lsPaymentRate1 = sqlStr("payment_rate1");
    if (!empty(lsPaymentRate1)) {
      wp.alertMesg +=
          "<script language='javascript'> alert('繳款評等：" + lsPaymentRate1 + "')</script>";
    }
    // -- emboss_reason
    String lsSubmsg = "", lsEmbossReason = "";
    if (lsCurrentCode.equals("0")) {
      lsEmbossReason = "2";
      wp.colSet("emboss_reason", "2");
    } else if (lsCurrentCode.equals("1")) {
      lsEmbossReason = "1";
      wp.colSet("emboss_reason", "1");
    } else if (lsCurrentCode.equals("2")) {
      lsEmbossReason = "1";
      wp.colSet("emboss_reason", "1");
    } else if (lsCurrentCode.equals("5")) {
      lsEmbossReason = "3";
      wp.colSet("emboss_reason", "3");
    } else {
      if (lsCurrentCode.equals("1")) {
        lsSubmsg = "申停";
      }
      if (lsCurrentCode.equals("3")) {
        lsSubmsg = "強停";
      }
      wp.alertMesg +=
          "<script language='javascript'> alert('此卡片之狀態碼為" + lsSubmsg + "不可做重製卡作業~')</script>";
      return -1;
    }
    sqlSelect = "select chi_name from crd_idno where id_p_seqno = :ls_id_p_seqno ";
    setString("ls_id_p_seqno", lsIdPSeqno);
    sqlSelect(sqlSelect);
    String lsChiName = sqlStr("chi_name");
    if (sqlRowNum <= 0) {
      wp.alertMesg += "<script language='javascript'> alert('卡人姓名抓取不到')</script>";
      return -1;
    }
    if (empty(lsGroupCode)) {
      lsGroupCode = "0000";
    }
    sqlSelect = "select spec_flag,card_mold_flag,service_type from ptr_group_card "
        + " where group_code = :ls_group_code " + " and card_type = :ls_card_type  ";
    setString("ls_group_code", lsGroupCode);
    setString("ls_card_type", lsCardType);
    sqlSelect(sqlSelect);
    String lsSpecFlag = sqlStr("spec_flag");
    String lsCardMoldFlag = sqlStr("card_mold_flag");
    String lsServiceType = sqlStr("service_type");
    if (sqlRowNum <= 0) {
      wp.alertMesg += "<script language='javascript'> alert('卡樣卡種抓取不到')</script>";
      return -1;
    }
    wp.colSet("mod_user", wp.loginUser);
    wp.colSet("mod_time", getSysDate());
    wp.colSet("card_type", lsCardType);

    // wp.col_set("kk_old_card_no", al_data);
    wp.colSet("apply_id", lsApplyId);
    wp.colSet("apply_id_code", lsApplyIdCode);
    wp.colSet("force_flag", lsForceFlag);
    wp.colSet("reg_bank_no", lsRegBankNo);
    wp.colSet("group_code", lsGroupCode);
    wp.colSet("unit_code", lsUnitCode);
    wp.colSet("spec_flag", lsSpecFlag);
    wp.colSet("chi_name", lsChiName);
    wp.colSet("corp_no", lsCorpNo);
    wp.colSet("eng_name", lsEngName);
    wp.colSet("emboss_4th_data", lsEmbossData);
    wp.colSet("current_code", lsCurrentCode);
    wp.colSet("oppost_date", lsOppostDate);
    wp.colSet("reissue_status", lsReissue3);
    wp.colSet("tt_reissue_status", commString.decode(lsReissue3, ",1,2,3,4", ",已登錄待製卡,製卡中,製卡完成,製卡失敗"));
    wp.colSet("change_status", lsChangeStatus);
    wp.colSet("expire_chg_flag", lsExpireChgFlag);
    wp.colSet("tt_expire_chg_flag", commString.decode(lsExpireChgFlag, ",1,2,3", ",系統不續卡,預約不續卡,人工不續卡"));
    wp.colSet("reissue_date", lsReissue1);
    wp.colSet("reissue_reason", lsReissue2);
    wp.colSet("tt_reissue_reason", commString.decode(lsReissue2, ",1,2,3,4", ",掛失,毀損,偽卡,一般停卡"));
    wp.colSet("is_old_reissue_date", lsReissue1);
    wp.colSet("is_old_reissue_reason", lsReissue2);
    wp.colSet("is_old_reissue_status", lsReissue3);
    wp.colSet("sup_flag", lsSupFlag);
    wp.colSet("tt_sup_flag", commString.decode(lsSupFlag, ",0,1", ",正卡,附卡"));
    wp.colSet("old_beg_date", lsNewBegDate);
    wp.colSet("old_end_date", lsNewEndDate);
    wp.colSet("db_new_card_no", lsNewCardNo);
    wp.colSet("ic_flag", lsIcFlag);
    wp.colSet("db_card_mold_flag", lsCardMoldFlag);
    wp.colSet("tt_db_card_mold_flag", commString.decode(lsCardMoldFlag, ",M,O,T", ",M:行動支付,O:其他,T:悠遊卡"));
    wp.colSet("service_type", lsServiceType);
    wp.colSet("bin_no", lsBinNo);
    wp.colSet("tt_service_type",
        commString.decode(lsServiceType, ",01,02,03,04", ",UICC,Micro SD卡,嵌入式,外接式"));
    if (lsCardMoldFlag.equals("M")) {
      sqlSelect = "SELECT se_id,msisdn,mno_id FROM mob_card a WHERE card_no = :ls_key1 "
          + " AND crt_date = (select max(crt_date) from mob_card where card_no = a.card_no) "
          + " fetch first 1 rows only ";
      setString("ls_key1", lsKey1);
      sqlSelect(sqlSelect);
      String lsSeId = sqlStr("se_id");
      String lsMsisdn = sqlStr("msisdn");
      String lsMnoId = sqlStr("mno_id");
      wp.colSet("se_id", lsSeId);
      wp.colSet("msisdn", lsMsisdn);
      wp.colSet("mno_id", lsMnoId);
      sqlSelect =
          "select wf_id,wf_id||'_'||wf_desc as tt_mno_id from ptr_sys_idtab where wf_id = :mno_id ";
      setString("mno_id", lsMnoId);
      sqlSelect(sqlSelect);
      wp.colSet("tt_mno_id", sqlStr("tt_mno_id"));
    }
    if (this.toNum(lsNewEndDate) < this.toNum(getSysDate())) {
      wp.alertMesg += "<script language='javascript'> alert('本卡片原效期不可小於系統日')</script>";
      return -1;
    }
    if (lsCurrentCode.equals("1") || lsCurrentCode.equals("2") || lsCurrentCode.equals("5")) {
      if (!empty(lsNewCardNo)) {
        wp.alertMesg += "<script language='javascript'> alert('此卡片已重製成功過,不可重複重製')</script>";
        return -1;
      }
    }
    String lsMajorValidTo = "";
    if (lsSupFlag.equals("1")) {
      sqlSelect = "select new_beg_date,new_end_date " + " from crd_card "
          + " where card_no = :ls_major_card_no ";
      setString("ls_major_card_no", lsMajorCardNo);
      sqlSelect(sqlSelect);
      String lsMajorValidFm = sqlStr("new_beg_date");
      lsMajorValidTo = sqlStr("new_end_date");
      if (sqlRowNum <= 0) {
        wp.alertMesg += "<script language='javascript'> alert('抓取不到正卡資料')</script>";
        return -1;
      }
      wp.colSet("major_card_no", lsMajorCardNo);
      wp.colSet("major_valid_fm", lsMajorValidFm);
      wp.colSet("major_valid_to", lsMajorValidTo);
      wp.colSet("pm_id", lsMajorId);
      wp.colSet("pm_id_code", lsMajorIdCode);
    } else {
      wp.colSet("pm_id", lsMajorId);
      wp.colSet("pm_id_code", lsMajorIdCode);
    }
    // --- 預約不續卡註記,效期不變
    if (!empty(lsExpireChgFlag)) {
      wp.colSet("valid_fm", lsNewBegDate);
      wp.colSet("valid_to", lsNewEndDate);
    } else {
      // -- 附卡需抓取正卡效期 2001/09/12
      if (lsSupFlag.equals("1")) {
        lsNewEndDate = lsMajorValidTo;
      }
      if (wfChkExpireDate(lsNewBegDate, lsNewEndDate, lsCardType, lsGroupCode, lsUnitCode) != 1) {
        return -1;
      }

    }
    if (wfChkOppost_date(lsEmbossReason, lsCurrentCode, lsOppostDate) != 1) {
      return -1;
    }
    ofcRetrieve(lsGroupCode, lsCardType);
    return 1;
  }

  int wfChkCardStatus(String alOldCardNo) {
    // -- 檢核卡檔現在狀態
    String lsOldCardNo = alOldCardNo;
    String sqlSelect = "select current_code,change_status,expire_chg_flag "
        + " ,oppost_date,reissue_status " + " from crd_card " + " where card_no = :ls_old_card_no ";
    setString("ls_old_card_no", lsOldCardNo);
    sqlSelect(sqlSelect);
    String lsCurrentCode = sqlStr("current_code");
    String lsChangeStatus = sqlStr("change_status");
    String lsExpireChgFlag = sqlStr("expire_chg_flag");
    String lsOppostDate = sqlStr("oppost_date");
    String lsReissueStatus = sqlStr("reissue_status");
    if (sqlRowNum <= 0) {
      wp.alertMesg += "<script language='javascript'> alert('抓取不到卡片檔資料')</script>";
      return -1;
    }
    wp.colSet("current_code", lsCurrentCode);
    wp.colSet("oppost_date", lsOppostDate);
    wp.colSet("reissue_status", lsReissueStatus);
    wp.colSet("tt_reissue_status",
        commString.decode(lsReissueStatus, ",1,2,3,4", ",已登錄待製卡,製卡中,製卡完成,製卡失敗"));
    wp.colSet("change_status", lsChangeStatus);
    /*
     * if(wf_get_spec_flag(wp.col_ss("group_code"),wp.col_ss("card_type"))!=1){ return -1; }
     */
    return 1;
  }

  void wfGetReissuedata() {
    String lsCardno = wp.colStr("old_card_no");
    String sqlSelect = "SELECT reissue_date, " + " reissue_reason, " + " reissue_status "
        + " FROM crd_card " + " WHERE card_no = :ls_cardno ";
    setString("ls_cardno", lsCardno);
    sqlSelect(sqlSelect);
    String lsVal1 = sqlStr("reissue_date");
    String lsVal2 = sqlStr("reissue_reason");
    String lsVal3 = sqlStr("reissue_status");
    if (sqlRowNum <= 0) {
      return;
    }
    wp.colSet("reissue_date", lsVal1);
    wp.colSet("reissue_reason", lsVal2);
    wp.colSet("tt_reissue_reason", commString.decode(lsVal2, ",1,2,3,4", ",掛失,毀損,偽卡,一般停卡"));
    wp.colSet("reissue_status", lsVal3);
    wp.colSet("tt_reissue_status", commString.decode(lsVal3, ",1,2,3,4", ",已登錄待製卡,製卡中,製卡完成,製卡失敗"));
  }

  int ofcPreretrieve() {
    isCardno = "";
    isAudcode = "";
    isCardno = wp.itemStr("kk_old_card_no");
    if (empty(isCardno)) {
      wp.alertMesg += "<script language='javascript'> alert('請輸入舊卡號')</script>";
      return -1;
    }
    String sqlSelect =
        "select decode(move_status,'','N',move_status) as ls_move_status from crd_card_ext where card_no = :is_cardno ";
    setString("is_cardno", isCardno);
    sqlSelect(sqlSelect);
    String lsMoveStatus = sqlStr("ls_move_status");
    if (lsMoveStatus.equals("Y")) {
      wp.alertMesg += "<script language='javascript'> alert('卡片正在 轉卡中 不可作業~ !!!')</script>";
      return -1;
    }

    return 1;
  }

  // 英文姓名check
  public boolean check(String str) {
    Pattern p = Pattern.compile("[^A-Z0-9/.,' -]");
    Matcher m = p.matcher(str);
    boolean match = m.find();
    if (match) {
      return false;
    }
    return true;
  }

  void ofcRetrieve(String groupCode, String cardType) {
    // -- 檢核是否為星座卡
    if (wfGetSpecFlag(groupCode, cardType) != 1) {
      return;
    }

    String sqlSelect = "select mob_status from mob_card where card_no = :is_cardno "
        + " and crt_date = (select max(crt_date) from mob_card  where card_no = :is_cardno) fetch first 1 rows only";
    setString("is_cardno", isCardno);
    sqlSelect(sqlSelect);
    String dbMobStatus = sqlStr("mob_status");
    wp.colSet("db_mob_status", dbMobStatus);
    wp.colSet("tt_db_mob_status",
        commString.decode(dbMobStatus, ",01,02,03,20", ",01:待客戶下載中,02:客戶已下載,03:客戶逾期未下載,20:卡片毀損重製"));

    sqlSelect = "select card_mold_flag from crd_card where card_no = :is_cardno ";
    setString("is_cardno", isCardno);
    sqlSelect(sqlSelect);
    String dbCardMoldFlag = sqlStr("card_mold_flag");
    wp.colSet("db_card_mold_flag", dbCardMoldFlag);
    wp.colSet("tt_db_card_mold_flag", commString.decode(dbCardMoldFlag, ",M,O,T", ",M:行動支付,O:其他,T:悠遊卡"));
  }
}
