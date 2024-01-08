/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-21  V1.00.00  Andy Liu   program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 107-09-13  V1.00.02  Andy		  update : UI                                *	
* 108-12-18  V1.00.03  Andy		  update : UI                                *	
* 109-05-06  V1.00.04 shiyuqi      updated for project coding standard      * 
 * 109-12-30  V1.00.05  shiyuqi       修改无意义命名                                                                                     *	
 * 112-07-17  V1.00.06  Wilson     刪除不存在table                               *
******************************************************************************/
package crdr01;

import java.io.InputStream;

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Crdr0090 extends BaseAction {

  InputStream inExcelFile = null;
  String mProgName = "crdr0090";

  String condWhere = "";
  String kkBatchno = "";
  String kkRecno = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml="
        + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // is_action="new";
      // clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
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
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      // wp.setExcelMode();
      xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      // wp.setExcelMode();
      pdfPrint();
    }

    dddwSelect();
    // init_button();
  }

  @Override
  public void clearFunc() throws Exception {
    wp.resetInputData();
    wp.resetOutputData();
  }

  private boolean getWhereStr() throws Exception {
    String exBatchno1 = wp.itemStr("ex_batchno1");
    String exBatchno2 = wp.itemStr("ex_batchno2");
    String exOnline = wp.itemStr("ex_online");
    String exComboFlag = wp.itemStr("ex_combo_flag");
    String exSource = wp.itemStr("ex_source");
    String exGroupCode = wp.itemStr("ex_group_code");
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exCheckResult = wp.itemStr("ex_check_result");

    String lsWhere = "where 1=1  ";
    if (empty(exBatchno1) == false) {
      lsWhere += " and batchno >= :ex_batchno1 ";
      setString("ex_batchno1", exBatchno1);
    }

    if (empty(exBatchno2) == false) {
      lsWhere += " and batchno <= :ex_batchno2 ";
      setString("ex_batchno2", exBatchno2);
    }

    if (exOnline.equals("Y")) {
      lsWhere += " and online_mark = '1' or online_mark='2' ";
    }

    if (exComboFlag.equals("Y")) {
      lsWhere += " and decode(combo_indicator,'','N',combo_indicator) !='N' ";
    }
    if (exComboFlag.equals("N")) {
      lsWhere += " and decode(combo_indicator,'','N',combo_indicator) = 'N' ";
    }

    if (exSource.equals("1")) {
      lsWhere += " and source = '1' ";
    }
    if (exSource.equals("2")) {
      lsWhere += " and source = '2' ";
    }

    if (empty(exGroupCode) == false) {
      lsWhere += " and group_code = :ex_group_code ";
      setString("ex_group_code", exGroupCode);
    }

    if (empty(exDateS) == false) {
      lsWhere += " and crt_date >= :exDateS ";
      setString("exDateS", exDateS);
    }
    if (empty(exDateE) == false) {
      lsWhere += " and crt_date <= :exDateE ";
      setString("exDateE", exDateE);
    }

    switch (exCheckResult) {
      case "1":
        lsWhere += " and (check_code = '0' and card_no <> '' and oth_chk_code ='0') ";
        break;
      case "2":
        lsWhere += " and (check_code != '0' or card_no ='' or  oth_chk_code !='0')";
        break;
    }

    wp.whereStr = lsWhere;
    setParameter();
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false)
      return;
    // cond_where = wp.whereStr + "";
    // wp.whereStr =cond_where;

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  private void setParameter() throws Exception {

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "" + "(batchno||'-'||recno) as wk_batchno, " + "batchno, " + "recno, "
        + "apply_no, " + "card_no, " + "chi_name, " + "eng_name, " + "birthday, "
        + "'' wk_apply_id, " + "apply_id, " + "apply_id_code, " + "'' wk_corp_no, " + "corp_no, "
        + "corp_no_code, " + "sex, " + "nation, " + "education, " + "marriage, "
        + "'' wk_aps_batchno, " + "aps_batchno, " + "aps_recno , " + "seqno, " + "reg_bank_no, "
        + "risk_bank_no, " + "act_no, " + "fee_code, " + "business_code, " + "group_code, "
        + "source_code, " + "valid_fm, " + "valid_to, " + "card_type, " + "credit_lmt, "
        + "force_flag, " + "vip, " + "(apply_id||'-'||apply_id_code) as wk_apply_id, " + "pm_id, "
        + "pm_id_code, " + "rel_with_pm, " + "value, " + "'' db_set, " + "''wk_mail_addr, "
        + "mail_zip, " + "mail_addr1, " + "mail_addr2, " + "mail_addr3, " + "mail_addr4, "
        + "mail_addr5, " + "home_area_code1, " + "home_tel_no1, " + "home_tel_ext1, "
        + "resident_zip, " + "resident_addr1, " + "resident_addr2, " + "resident_addr3, "
        + "resident_addr4, " + "resident_addr5, " + "resident_no, " + "passport_no, "
        + "salary_code, " + "home_area_code2, " + "home_tel_no2, " + "home_tel_ext2, "
        + "company_name, " + "job_position, " + "cellar_phone, " + "office_area_code1, "
        + "office_tel_no1, " + "office_tel_ext1, " + "office_area_code2, " + "office_tel_no2, "
        + "office_tel_ext2, " + "corp_assure_flag, " + "corp_act_flag, " + "sup_flag, "
        + "stmt_cycle, " + "mail_type, " + "cardcat, " + "introduce_no, " + "introduce_id, "
        + "introduce_name, " + "credit_flag, " + "comm_flag,source, " + "'' db_message, "
        + "online_mark, " + "emboss_4th_data, " + "e_mail_addr, " + "member_id, "
        + "other_cntry_code, " + "son_card_flag, " + "org_indiv_crd_lmt, " + "branch, "
        + "mail_attach1, " + "mail_attach2, " + "ic_flag, " + "service_year, " + "pm_cash, "
        + "sup_cash, " + "contactor1_name, " + "contactor1_relation, " + "contactor1_area_code, "
        + "contactor1_tel, " + "contactor1_ext, " + "est_graduate_month, " + "vacation_code, "
        + "market_agree_base, " + "market_agree_act, " + "fancy_limit_flag, " + "salary, "
        + "contactor2_name, " + "contactor2_relation, " + "contactor2_area_code, "
        + "contactor2_tel, " + "contactor2_ext, " + "student, " + "check_code, "
        + "decode(check_code,'0','',(select msg from crd_message where 1=1 and msg_type = 'NEW_CARD' and msg_value = crd_emap_tmp.check_code)) db_msg, "
        // + "check_code||':'||(select wf_desc from ptr_sys_idtab where wf_type ='CHECK_CODE' and
        // wf_id = crd_emap_tmp.check_code) as db_check_code, "
        + "cardno_code, " + "oth_chk_code," + "'*****' dex_code1, " + "'*****' dex_bank1, "
        + "'*****' dex_card1, " + "0 dex_amt1, " + "'*****' dex_product1, " + "'*****' dex_code2, "
        + "'*****' dex_bank2, " + "'*****' dex_card2, " + "0 dex_amt2, " + "'*****' dex_product2, "
        + "'*****' dex_code3, " + "'*****' dex_bank3, " + "'*****' dex_card3, " + "0 dex_amt3, "
        + "'*****' dex_product3, " + "'' wk_temp";

    wp.daoTable = " crd_emap_tmp ";
    wp.whereOrder = " order by batchno asc ";

    // setParameter();
    // System.out.println("select " + wp.selectSQL + " from " +wp.daoTable+wp.whereStr);
    // wp.daoTable);

    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    pageQuery();
    // list_wkdata();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      wp.colSet("ft_cnt", "0");
      wp.colSet("db_ok", "0");
      wp.colSet("db_fail", "0");
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
    wp.setPageValue();
    // list_wkdata();
  }

  @Override
  public void dataRead() throws Exception {
    kkBatchno = itemkk("data_k1");
    kkRecno = itemkk("data_k2");
    wp.selectSQL = "" + "batchno, " + "recno, " + "(batchno||'-'||recno) as wk_batchno, "
        + "apply_no, " + "card_no, " + "chi_name, " + "eng_name, " + "birthday, "
        + "(apply_id||'-'||apply_id_code) as wk_apply_id, " + "apply_id, " + "apply_id_code, "
        + "decode(corp_no,'','',(corp_no||'-'||corp_no_code)) as wk_corp_no, " + "corp_no, "
        + "corp_no_code, " + "sex, " + "nation, " + "education, " + "marriage, "
        + "(aps_batchno||'-'||aps_recno) as wk_aps_batchno, " + "aps_batchno, " + "aps_recno , "
        + "seqno, " + "reg_bank_no, " + "risk_bank_no, " + "act_no, " + "fee_code, "
        + "business_code, " + "group_code, " + "source_code, " + "valid_fm, " + "valid_to, "
        + "card_type, " + "credit_lmt, " + "force_flag, " + "vip, "
        + "(pm_id||'-'||pm_id_code) as wk_pm_id, " + "pm_id, " + "pm_id_code, " + "rel_with_pm, "
        + "value, " + "'' db_set, " + "''wk_mail_addr, " + "mail_zip, " + "mail_addr1, "
        + "mail_addr2, " + "mail_addr3, " + "mail_addr4, " + "mail_addr5, " + "home_area_code1, "
        + "home_tel_no1, " + "home_tel_ext1, " + "resident_zip, " + "resident_addr1, "
        + "resident_addr2, " + "resident_addr3, " + "resident_addr4, " + "resident_addr5, "
        + "resident_no, " + "passport_no, " + "salary_code, " + "home_area_code2, "
        + "home_tel_no2, " + "home_tel_ext2, " + "company_name, " + "job_position, "
        + "cellar_phone, " + "office_area_code1, " + "office_tel_no1, " + "office_tel_ext1, "
        + "office_area_code2, " + "office_tel_no2, " + "office_tel_ext2, " + "corp_assure_flag, "
        + "corp_act_flag, " + "sup_flag, " + "stmt_cycle, " + "mail_type, " + "cardcat, "
        + "introduce_no, " + "introduce_id, " + "introduce_name, " + "credit_flag, "
        + "comm_flag,source, " + "'' db_message, " + "online_mark, " + "emboss_4th_data, "
        + "e_mail_addr, " + "member_id, " + "other_cntry_code, " + "son_card_flag, "
        + "org_indiv_crd_lmt, " + "branch, " + "mail_attach1, " + "mail_attach2, " + "ic_flag, "
        + "service_year, " + "pm_cash, " + "sup_cash, " + "contactor1_name, "
        + "contactor1_relation, " + "contactor1_area_code, " + "contactor1_tel, "
        + "contactor1_ext, " + "est_graduate_month, " + "vacation_code, " + "market_agree_base, "
        + "market_agree_act, " + "fancy_limit_flag, " + "salary, " + "contactor2_name, "
        + "contactor2_relation, " + "contactor2_area_code, " + "contactor2_tel, "
        + "contactor2_ext, " + "student, " + "check_code, " + "cardno_code, "
        + "decode(check_code,'0','',(select msg from crd_message where 1=1 and msg_type = 'NEW_CARD' and msg_value = crd_emap_tmp.check_code)) db_msg, "
        // + "check_code||':'||(select wf_desc from ptr_sys_idtab where wf_type ='CHECK_CODE' and
        // wf_id = crd_emap_tmp.check_code) as db_check_code, "
        + "oth_chk_code," + "'' dex_code1, " + "'' dex_bank1, " + "'' dex_card1, " + "0 dex_amt1, "
        + "'' dex_product1, " + "'' dex_code2, " + "'' dex_bank2, " + "'' dex_card2, "
        + "0 dex_amt2, " + "'' dex_product2, " + "'' dex_code3, " + "'' dex_bank3, "
        + "'' dex_card3, " + "0 dex_amt3, " + "'' dex_product3, " + "'' wk_temp ";
    wp.daoTable = " crd_emap_tmp ";
    wp.whereStr = "where 1=1 ";
    wp.whereStr += " and batchno = :batchno ";
    wp.whereStr += " and recno = :recno ";
    setString("batchno", kkBatchno);
    setString("recno", kkRecno);

    // System.out.println("select " + wp.selectSQL + " from " +wp.daoTable+wp.whereStr);

    pageSelect();

    if (sqlNotFind()) {
      alertErr("查無明細資料, batchno = " + kkBatchno);
      return;
    }
    listWkdata();

  }

  void listWkdata() throws Exception {
    int rowCt = 0, dbOk = 0, dbFail = 0;
    String wkBatchno = "", wkApplyId = "", wkCorpNo = "", wkApsBatchno = "", wkPmID = "",
        dbSet = "", wkMailAddr = "";
    String wkHomeTel1 = "", wkResidentAddr = "", wkHomeTel2 = "", wkOfficeTel1 = "", wkOfficeTel2,
        dbMessage = "";
    String wpCheckCode = "", wpCardnoCode = "", wpOthChkCode = "", wpBatchno = "", wpRecno = "";

    String lsSql = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wpBatchno = wp.colStr(ii, "batchno");
      wpRecno = wp.colStr(ii, "recno");
      // 計算欄位
      rowCt += 1;
      // sex性別
      switch (wp.colStr(ii, "sex")) {
        case "1":
          wp.colSet(ii, "sex", "男");
          break;
        case "2":
          wp.colSet(ii, "sex", "女");
          break;
      }
      // nation國籍
      switch (wp.colStr(ii, "nation")) {
        case "1":
          wp.colSet(ii, "nation", "本國");
          break;
        case "2":
          wp.colSet(ii, "nation", "外國");
          break;
      }
      // education教育
      switch (wp.colStr(ii, "education")) {
        case "1":
          wp.colSet(ii, "education", "博士");
          break;
        case "2":
          wp.colSet(ii, "education", "碩士");
          break;
        case "3":
          wp.colSet(ii, "education", "大學");
          break;
        case "4":
          wp.colSet(ii, "education", "專科");
          break;
        case "5":
          wp.colSet(ii, "education", "高中高職");
          break;
        case "6":
          wp.colSet(ii, "education", "其他");
          break;
      }
      // marriage婚姻
      switch (wp.colStr(ii, "marriage")) {
        case "1":
          wp.colSet(ii, "marriage", "已婚");
          break;
        case "2":
          wp.colSet(ii, "marriage", "未婚");
          break;
        case "3":
          wp.colSet(ii, "marriage", "離婚");
          break;
      }
      // source_code來源中文
      switch (wp.colStr(ii, "source_code")) {
        case "1":
          wp.colSet(ii, "source_code", "線上製卡");
          break;
        case "2":
          wp.colSet(ii, "source_code", "緊急製卡");
          break;
      }
      // rel_with_pm關係
      switch (wp.colStr(ii, "rel_with_pm")) {
        case "本人":
          break;
        case "1":
          wp.colSet(ii, "rel_with_pm", "配偶");
          break;
        case "2":
          wp.colSet(ii, "rel_with_pm", "父母");
          break;
        case "3":
          wp.colSet(ii, "rel_with_pm", "子女");
          break;
        case "4":
          wp.colSet(ii, "rel_with_pm", "兄弟姊妹");
          break;
        case "5":
          wp.colSet(ii, "rel_with_pm", "法人");
          break;
        case "6":
          wp.colSet(ii, "rel_with_pm", "其他");
          break;
      }
      // db_set---- 電子錢包
      if (wp.colStr(ii, "apply_id").equals(wp.colStr(ii, "pm_id"))) {
        if (wp.colStr(ii, "pm_cash").equals("A")) {
          dbSet = wp.colStr(ii, "pm_cash");
        }
      } else {
        if (wp.colStr(ii, "sup_cash").equals("A")) {
          dbSet = wp.colStr(ii, "sup_cash");
        }
      }
      wp.colSet(ii, "db_set", dbSet);
      // wk_mail_addr通訊地址
      wkMailAddr = wp.colStr(ii, "mail_zip") + " " + wp.colStr(ii, "mail_addr1") + " "
          + wp.colStr(ii, "mail_addr2") + " " + wp.colStr(ii, "mail_addr3") + " "
          + wp.colStr(ii, "mail_addr4") + " " + wp.colStr(ii, "mail_addr5");
      wp.colSet(ii, "wk_mail_addr", wkMailAddr);
      // wk_home_tel_1住家電話(一)
      wkHomeTel1 = wp.colStr(ii, "home_area_code1") + " " + wp.colStr(ii, "home_tel_no1") + " "
          + wp.colStr(ii, "home_tel_ext1");
      wp.colSet(ii, "wk_home_tel_1", wkHomeTel1);
      // wk_resident_addr戶籍地
      wkResidentAddr = wp.colStr(ii, "resident_zip") + " " + wp.colStr(ii, "resident_addr1") + " "
          + wp.colStr(ii, "resident_addr2") + " " + wp.colStr(ii, "resident_addr3") + " "
          + wp.colStr(ii, "resident_addr4") + " " + wp.colStr(ii, "resident_addr5");
      wp.colSet(ii, "wk_resident_addr", wkResidentAddr);
      // wk_home_tel_2住家電話(二)
      wkHomeTel2 = wp.colStr(ii, "home_area_code2") + " " + wp.colStr(ii, "home_tel_no2") + " "
          + wp.colStr(ii, "home_tel_ext2");
      wp.colSet(ii, "wk_home_tel_2", wkHomeTel2);
      // wk_office_tel_1公司電話號碼(一)
      wkOfficeTel1 = wp.colStr(ii, "office_area_code1") + " " + wp.colStr(ii, "office_tel_no1")
          + " " + wp.colStr(ii, "office_tel_ext1");
      wp.colSet(ii, "wk_office_tel_1", wkOfficeTel1);
      // wk_office_tel_2公司電話號碼(二)
      wkOfficeTel2 = wp.colStr(ii, "office_area_code2") + " " + wp.colStr(ii, "office_tel_no2")
          + " " + wp.colStr(ii, "office_tel_ext2");
      wp.colSet(ii, "wk_office_tel_2", wkOfficeTel2);
      // corp_act_flag總繳/個繳
      switch (wp.colStr(ii, "corp_act_flag")) {
        case "Y":
          wp.colSet(ii, "corp_act_flag", "總繳");
          break;
        case "N":
          wp.colSet(ii, "corp_act_flag", "個繳");
          break;
      }
      // sup_flag正/附卡
      switch (wp.colStr(ii, "sup_flag")) {
        case "0":
          wp.colSet(ii, "sup_flag", "正卡");
          break;
        case "1":
          wp.colSet(ii, "sup_flag", "附卡");
          break;
      }
      // mail_type寄件別
      switch (wp.colStr(ii, "mail_type")) {
        case "1":
          wp.colSet(ii, "mail_type", "1-普通掛號");
          break;
        case "2":
          wp.colSet(ii, "mail_type", "2-限時掛號");
          break;
        case "3":
          wp.colSet(ii, "mail_type", "3-自取");
          break;
        case "4":
          wp.colSet(ii, "mail_type", "4-分行");
          break;
        case "5":
          wp.colSet(ii, "mail_type", "5-退件");
          break;
        case "6":
          wp.colSet(ii, "mail_type", "Q-非當日處理");
          break;
      }
      // source資料來源
      switch (wp.colStr(ii, "source")) {
        case "1":
          wp.colSet(ii, "source", "新製卡");
          break;
        case "2":
          wp.colSet(ii, "source", "普昇金卡");
          break;
        case "3":
          wp.colSet(ii, "source", "整批續卡");
          break;
        case "4":
          wp.colSet(ii, "source", "提前續卡");
          break;
        case "5":
          wp.colSet(ii, "source", "重製");
          break;
      }
      // db_message
      // if dw_report.object.check_code[L] > '' and dw_report.object.check_code[L] <> '0' then
      // dw_report.object.db_message[L] = wf_get_message("NEW_CARD",dw_report.object.check_code[L])
      // end if
      // if dw_report.object.cardno_code[L] > '' and dw_report.object.cardno_code[L] <> '0' then
      // dw_report.object.db_message[L] = wf_get_message("CARDNO",dw_report.object.cardno_code[L])
      // end if
      // if dw_report.object.oth_chk_code[L] > '' and dw_report.object.oth_chk_code[L] <> '0' then
      // dw_report.object.db_message[L] =
      // wf_get_message("STAR_CARD",dw_report.object.oth_chk_code[L])
      // end if

      wpCheckCode = wp.colStr(ii, "check_code");
      if (wpCheckCode.equals("0")) {
        dbMessage = "成功";
      }
      if (wpCheckCode.equals("") == false && wpCheckCode.equals("0") == false) {
        lsSql = "select msg from crd_message ";
        lsSql += " where msg_type = 'NEW_CARD' ";
        lsSql += " and msg_value = :wk_check_code ";
        setString("wk_check_code", wpCheckCode);
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          dbMessage = sqlStr("msg");
        } else {
          dbMessage = "失敗";
        }
      }
      wpCardnoCode = wp.colStr(ii, "cardno_code");
      if (wpCardnoCode.equals("") == false && wpCardnoCode.equals("0") == false) {
        lsSql = "select msg from crd_message ";
        lsSql += " where msg_type = 'CARDNO' ";
        lsSql += " and msg_value = :wk_cardno_code ";
        setString("wk_cardno_code", wpCardnoCode);
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          dbMessage = sqlStr("msg");
        } else {
          dbMessage = "失敗";
        }
      }
      wpOthChkCode = wp.colStr(ii, "oth_chk_code");
      if (wpOthChkCode.equals("") == false && wpOthChkCode.equals("0") == false) {
        lsSql = "select msg from crd_message ";
        lsSql += " where msg_type = 'STAR_CARD' ";
        lsSql += " and msg_value = :wk_oth_chk_code ";
        setString("wk_oth_chk_code", wpOthChkCode);
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          dbMessage = sqlStr("msg");
        } else {
          dbMessage = "失敗";
        }
      }
      wp.colSet(ii, "db_message", dbMessage);

      // online_mark線上製卡
      switch (wp.colStr(ii, "online_mark")) {
        case "0":
          wp.colSet(ii, "online_mark", "一般卡");
          break;
        case "1":
          wp.colSet(ii, "online_mark", "線上製卡");
          break;
        case "2":
          wp.colSet(ii, "online_mark", "緊急製卡");
          break;
      }

      // pm_cash申請電子錢包正卡
      switch (wp.colStr(ii, "pm_cash")) {
        case "0":
          wp.colSet(ii, "pm_cash", "Y");
          break;
        case "1":
          wp.colSet(ii, "pm_cash", "N");
          break;
      }

      // pm_cash申請電子錢包附卡
      switch (wp.colStr(ii, "sup_cash")) {
        case "0":
          wp.colSet(ii, "sup_cash", "Y");
          break;
        case "1":
          wp.colSet(ii, "sup_cash", "N");
          break;
      }

      // contactor1_relation聯絡人關係
      switch (wp.colStr(ii, "contactor1_relation")) {
        case "1":
          wp.colSet(ii, "contactor1_relation", "親戚");
          break;
        case "2":
          wp.colSet(ii, "contactor1_relation", "朋友");
          break;
      }

      // contactor2_relation聯絡人關係
      switch (wp.colStr(ii, "contactor2_relation")) {
        case "1":
          wp.colSet(ii, "contactor2_relation", "親戚");
          break;
        case "2":
          wp.colSet(ii, "contactor2_relation", "朋友");
          break;
      }

      // 成功失敗筆數
      if (wpCheckCode.equals("0")) {
        dbOk += 1;
      } else {
        dbFail += 1;
      }
    }
    wp.colSet("row_ct", intToStr(rowCt));
    wp.colSet("db_ok", intToStr(dbOk));
    wp.colSet("db_fail", intToStr(dbFail));
    wp.colSet("user_id", wp.loginUser);
  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      String exBatchno1 = wp.itemStr("ex_batchno1");
      String exOnline = wp.itemStr("ex_online");
      String exComboFlag = wp.itemStr("ex_combo_flag");
      String exSource = wp.itemStr("ex_source");
      String exGroupCode = wp.itemStr("ex_group_code");
      String exDateS = wp.itemStr("exDateS");
      String exDateE = wp.itemStr("exDateE");
      String exCheckResult = wp.itemStr("ex_check_result");
      switch (exSource) {
        case "0":
          exSource = "全部";
          break;
        case "1":
          exSource = "新製卡";
          break;
        case "2":
          exSource = "普昇金";
          break;
      }
      switch (exCheckResult) {
        case "0":
          exCheckResult = "全部";
          break;
        case "1":
          exCheckResult = "成功";
          break;
        case "2":
          exCheckResult = "不成功";
          break;
      }
      String cond1 =
          "批號 :" + exBatchno1 + "~" + exBatchno1 + " 線上製卡: " + exOnline + " COMBO卡: " + exComboFlag;
      wp.colSet("cond_1", cond1);
      String cond2 = "製卡來源: " + exSource + " 團體代碼: " + exGroupCode + "產生日期:" + exDateS + " ~ "
          + exDateE + "檢核結果:" + exCheckResult;
      wp.colSet("cond_2", cond2);
      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = mProgName + ".xlsx";

      // ====================================
      // -明細-
      xlsx.sheetName[0] = "明細";
      queryFunc();
      wp.setListCount(1);
      log("Detl: rowcnt:" + wp.listCount[0]);
      xlsx.processExcelSheet(wp);
      /*
       * //-合計- xlsx.sheetName[1] ="合計"; query_Summary(cond_where); wp.listCount[1] =sql_nrow;
       * ddd("Summ: rowcnt:" + wp.listCount[1]); //xlsx.sheetNo = 1; xlsx.processExcelSheet(wp);
       */
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");

    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }
  }

  void pdfPrint() throws Exception {
    wp.reportId = mProgName;
    // -cond-
    String cond1 = "PDFTEST: ";
    wp.colSet("cond_1", cond1);
    wp.pageRows = 99999;

    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 1; // 一頁1筆
    pdf.procesPDFreport(wp);

    pdf = null;
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub
    dataRead();
  }

  @Override
  public void dddwSelect() {
    try {
      // dddw_group_code
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_group_code");
      dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name",
          "where 1=1 group by group_code,group_name order by group_code");
    } catch (Exception ex) {
    }
  }

  @Override
  public void userAction() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}

