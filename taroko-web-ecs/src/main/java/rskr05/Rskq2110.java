package rskr05;
/**
 * 2019-1203   JH    UAT
 * 2019-0624:  JH    p_xxx >>acno_p_xxx
 * 2019-1121:  Alex  fixed queryRead and dataRead
 * 109-04-28  V1.00.03  Tanwei       updated for project coding standard
 * 109-12-30  V1.00.05  shiyuqi       修改无意义命名                                                                                     *
* */
import ofcapp.BaseAction;

public class Rskq2110 extends BaseAction {
  String rowid = "";

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
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    }

    dddwSelect();
    initButton();

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
      alertErr2("匯入日期起迄：輸入錯誤");
      return;
    }

    if (wp.itemEmpty("ex_crt_date1") && wp.itemEmpty("ex_crt_date2") && wp.itemEmpty("ex_card_no")
        && wp.itemEmpty("ex_idno") && wp.itemEq("ex_from_type", "0")) {
      alertErr2("請輸入查詢條件");
      return;
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_crt_date1"), "A.crt_date", ">=")
        + sqlCol(wp.itemStr("ex_crt_date2"), "A.crt_date", "<=")
        + sqlCol(wp.itemStr("ex_card_no"), "B.card_no") + sqlCol(wp.itemStr("ex_idno"), "A.id_no");

    if (wp.itemEq("ex_proc_flag", "Y")) {
      lsWhere += " and A.proc_date <> ''";
    } else if (wp.itemEq("ex_proc_flag", "N")) {
      lsWhere += " and A.proc_date = ''";
    }
    String lsType = wp.itemStr2("ex_from_type");
    if (eqIgno(lsType, "1")) {
      // <option value="1" ${ex_from_type-1}>1.支票拒往</option>
      lsWhere += " and A.from_type in ('1','11','12')";
    } else if (eqIgno(lsType, "3")) {
      // <option value="3" ${ex_from_type-3}>3.他行強停-JCIC</option>
      lsWhere += " and A.from_type in ('3','31')";
    } else if (eqIgno(lsType, "4")) {
      // <option value="4" ${ex_from_type-4}>3.人工匯入</option>
      lsWhere += " and A.from_type in ('4','41')";
    } else if (eqIgno(lsType, "5")) {
      // <option value="5" ${ex_from_type-5}>4.人工登錄</option>
      lsWhere += " and A.from_type in ('5','51')";
    }

    if (wp.itemEq("ex_card_num", "Y")) {
      lsWhere += " and (B.sup0_card_num>0 or B.sup1_card_num>0 or B.corp_card_num>0)";
    } else if (wp.itemEq("ex_card_num", "N")) {
      lsWhere += " and (B.sup0_card_num=0 and B.sup1_card_num=0 and B.corp_card_num=0)";
    }

    if (wp.itemEq("ex_block_flag", "Y")) {
      lsWhere += " and B.proc_reason<>''";
    } else if (wp.itemEq("ex_block_flag", "N")) {
      lsWhere += " and B.proc_reason =''";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }


  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " A.crt_date, " + " A.from_type, " + " A.id_no, " + " A.corp_no, "
        + " A.chi_name, " + " A.imp_file, " + " A.annou_type, " + " A.stop_reason, "
        + " A.stop_date, " + " A.bank_no, " + " A.bank_name, "
        // + " A.block_reason4, "
        + " A.id_code_rows," + " hex(A.rowid) as rowid, " + " B.acct_type , "
        + " B.block_reason as block_reason1," + " B.block_reason2 ," + " B.block_reason3 ,"
        + " B.block_reason4 ," + " B.block_reason5 , " + " B.proc_reason , " + " B.sup0_card_num , "
        + " B.sup1_card_num , " + " B.corp_card_num ";
    wp.daoTable = "rsk_bad_annou A left join rsk_bad_annou_log B "
        + " on A.crt_date = B.crt_date and A.from_type = B.from_type "
        + " and A.id_p_seqno = B.id_p_seqno and A.corp_p_seqno = B.corp_p_seqno "
        + " and A.major_id_p_seqno =B.major_id_p_seqno ";
    wp.whereOrder = " order by A.crt_date ";
    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    queryAfter(wp.listCount[0]);
    wp.setPageValue();
  }

  void queryAfter(int llNrow) {
    for (int ii = 0; ii < llNrow; ii++) {
      wp.colSet(ii, "tt_from_type", ttFromType(wp.colStr(ii, "from_type")));
      wp.colSet(ii, "tt_annou_type", deCodeAnnouType(wp.colStr(ii, "annou_type")));
      String lsBlock = "";
      for (int nn = 1; nn < 6; nn++) {
        if (wp.colEmpty(ii, "block_reason" + nn))
          continue;
        lsBlock += "," + wp.colStr(ii, "block_reason" + nn);
      }
      wp.colSet(ii, "wk_block_reason", commString.mid(lsBlock, 1));

      // -jh-191203-
      if (wp.colEmpty(ii, "acct_type")) {
        wp.colSet(ii, "sup0_card_num", "");
        wp.colSet(ii, "sup1_card_num", "");
        wp.colSet(ii, "corp_card_num", "");
      }
    }
  }

  @Override
  public void querySelect() throws Exception {
    rowid = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    wp.selectSQL = "crt_date, " + "from_type, " + "id_no, " + "corp_no, " + "chi_name, "
        + "imp_file, " + "annou_type, " + "stop_reason, " + "stop_date, " + "bank_no, "
        + "bank_name, " + "block_reason4, " + "id_code_rows," + "id_p_seqno," + "corp_p_seqno , "
        + "crt_user , " + "mod_user , " + "to_char(mod_time,'yyyymmdd') as mod_date ";
    wp.daoTable = "rsk_bad_annou";
    wp.whereStr = " where rowid =?";
    setRowid(1, rowid);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + rowid);
      return;
    }

    wp.colSet("tt_from_type", ttFromType(wp.colStr("from_type")));

    selectLog();
  }

  void selectLog() {
    wp.selectSQL = "kind_flag ," + " acct_type," + " uf_acno_key(acno_p_seqno) as acct_key ,"
        + " card_no," + " block_reason," + " block_reason2," + " block_reason3," + " block_reason4,"
        + " block_reason5," + " proc_reason," + " sup_card_flag," + " bad_loan_flag,"
        + " block_flag," + " proc_date," + " m_code," + " acno_p_seqno," + " id_p_seqno,"
        + " major_id_p_seqno ";
    wp.daoTable = "rsk_bad_annou_log";
    wp.whereStr = " where 1=1" + sqlCol(wp.colStr("crt_date"), "crt_date")
        + sqlCol(wp.colStr("from_type"), "from_type")
        + sqlCol(wp.colStr("id_p_seqno"), "id_p_seqno")
        + sqlCol(wp.colStr("corp_p_seqno"), "corp_p_seqno");
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無明細");
      return;
    }
    selectLogAfter();
  }

  void selectLogAfter() {
    String lsBlockReason = "";
    if (!wp.colEmpty("block_reason")) {
      if (lsBlockReason.length() == 0)
        lsBlockReason += wp.colStr("block_reason");
      else
        lsBlockReason += "," + wp.colStr("block_reason");
    }
    if (!wp.colEmpty("block_reason2")) {
      if (lsBlockReason.length() == 0)
        lsBlockReason += wp.colStr("block_reason2");
      else
        lsBlockReason += "," + wp.colStr("block_reason2");
    }
    if (!wp.colEmpty("block_reason3")) {
      if (lsBlockReason.length() == 0)
        lsBlockReason += wp.colStr("block_reason3");
      else
        lsBlockReason += "," + wp.colStr("block_reason3");
    }
    if (!wp.colEmpty("block_reason4")) {
      if (lsBlockReason.length() == 0)
        lsBlockReason += wp.colStr("block_reason4");
      else
        lsBlockReason += "," + wp.colStr("block_reason4");
    }
    if (!wp.colEmpty("block_reason5")) {
      if (lsBlockReason.length() == 0)
        lsBlockReason += wp.colStr("block_reason5");
      else
        lsBlockReason += "," + wp.colStr("block_reason5");
    }
    wp.colSet("wk_block_reason", lsBlockReason);
  }

  String ttFromType(String type) {
    return commString.decode(type,
        new String[] {"1", "11", "12", "2", "21", "3", "31", "4", "41", "5", "51"},
        new String[] {"支票拒往", "支票拒往[附卡]", "支票拒往[公司負責人]", "他行強停-NCCC", "他行強停-NCCC[附卡]", "他行強停-JCIC",
            "他行強停-JCIC[附卡]", "人工匯入", "人工匯入[附卡]", "人工登錄", "人工登錄[附卡]"});
    /*
     * +"支票拒往~t1" & +"/支票拒往[附卡]~t11" & +"/支票拒往[公司負責人]~t12" & +"/他行強停-NCCC~t2" &
     * +"/他行強停-NCCC[附卡]~t21" & +"/他行強停-JCIC~t3" & +"/他行強停-JCIC[附卡]~t31" & +"/人工匯入~t4" &
     * +"/人工匯入[附卡]~t41" & +"/人工登錄~t5" & +"/人工登錄[附卡]~t51" &
     */
  }

  String deCodeAnnouType(String type) {
    return commString.decode(type, new String[] {"1", "2", "3", "4", "5", "6", "9"},
        new String[] {"他行強停", "支票拒往", "失業/收入不足", "親屬代償訊息", "授信異常", "退票", "其他"});

    // return "1.他行強停" &
    // +"/2.支票拒往~t2" &
    // +"/3.失業/收入不足~t3" &
    // +"/4.親屬代償訊息~t4" &
    // +"/5.授信異常~t5" &
    // +"/6.退票~t6" &
    // +"/9.其他~t9" &
    // +"'"
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
