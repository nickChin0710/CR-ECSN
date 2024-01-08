/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-04  V1.00.00  Andy         program initial                            *
* 106-12-14  V1.00.01  Andy		     update : ucStr==>zzstr                     *
* 107-07-31  V1.00.01  Andy		     update : add col digital_flag              *
* 108-12-27  V1.00.02  JustinWu  add initPage()
* 109-04-22  V1.00.02  yanghan  修改了變量名稱和方法名稱*
* 109-06-22  V1.00.02 shiyuqi      讀取table : ptr_branch 改為gen_brn           *
* 109-12-25  V1.00.03 Justin         parameterize sql
 *  109/12/30  V1.00.04    Zuwei       “卡務中心”改為”信用卡部”   *
 *  110/1/4  V1.00.05  yanghan       修改了變量名稱和方法名稱            *
* 110-10-19  V1.00.06  YangBo	  joint sql replace to parameters way        *
* 111-09-16  V1.00.07   Ryan      調整寄件別、卡片寄送地址                                                                    *  
* 112-05-30  V1.00.08   Ryan      掛號號碼改成使用者不可輸入,索引欄位增加身份證字號,查詢欄位在最左邊增加退卡編號、掛號條碼 *  
* 112-06-05  V1.00.09   Ryan      增加卡片寄送地址註記欄位、處理邏輯
******************************************************************************/

package dbcm01;

import busi.SqlPrepare;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;

public class Dbcm0160 extends BaseEdit {
  String lsMsg = "";
  String exCardNo = "";
  String exIdpSeqno = "";
  String idSeqno;
  String pSeqno;
  String exReturnDate;
  String[] arrayMsg = null;
  String[] arrayMsgValue = null;
  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;

    switch (strAction) {
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
      case "R1":
        // -Log讀取-
        strAction = "R1";
        dataRead1();
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
      case "C":
        /* 查詢 by card_no, mail_no, or barcode_num */
        itemchanged();
        break;
      case "AJAX":
      	getChiAddr("AJAX");
      	break;
	  case "B1":
		/* 清畫面 */
		strAction = "new";
		clearFunc();
		break;
	  case "UPLOAD":
		procFunc();
		break;
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("return_date", getSysDate());
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
	sqlParm.clear();
    wp.whereStr = " where 1=1 ";
    String cardNo = wp.itemStr("ex_card_no");
    String exAcctNo = wp.itemStr("ex_acct_no");
    String exReturnSeqno = wp.itemStr("ex_return_seqno");
    String exBarcodeNum = wp.itemStr("ex_barcode_num");
    String exReturnDate1 = wp.itemStr("ex_return_date1");
    String exReturnDate2 = wp.itemStr("ex_return_date2");
	String exIdNo = wp.itemStr("ex_id_no");
    int theNumOfEmpty = 7;
    if (!empty(cardNo)) {
      	wp.whereStr += " and a.card_no = :ls_card_no ";
    	setString("ls_card_no",cardNo);
      theNumOfEmpty--;
    }
    if (!empty(exAcctNo)) {
    	wp.whereStr += " and a.acct_no = :ls_acct_no ";
    	setString("ls_acct_no",exAcctNo);
      theNumOfEmpty--;
    }
    if (!empty(exBarcodeNum)) {
  		wp.whereStr += " and a.barcode_num = :ls_barcode_num ";
		setString("ls_barcode_num",exBarcodeNum);
      theNumOfEmpty--;
    }
    if (!empty(exReturnSeqno)) {
      	wp.whereStr += " and a.return_seqno = :ls_return_seqno ";
		setString("ls_return_seqno",exReturnSeqno);
      theNumOfEmpty--;
    }
    if (!empty(exReturnDate1)) {
        wp.whereStr += " and a.return_date >= :ls_return_date1 ";
  		setString("ls_return_date1",exReturnDate1);
        theNumOfEmpty--;
      }
    if (!empty(exReturnDate2)) {
        wp.whereStr += " and a.return_date <= :ls_return_date2 ";
  		setString("ls_return_date2",exReturnDate2);
        theNumOfEmpty--;
      }
	if (!empty(exIdNo)) {
		wp.whereStr += " and b.id_no = :ls_id_no ";
		setString("ls_id_no", exIdNo);
		theNumOfEmpty--;
	}
    if (theNumOfEmpty == 7) {
      alertErr("退卡日期、卡號、金融帳號、退卡編號、身份證字號以及掛號條碼至少填寫一項！");
      return false;
    } else {
      return true;
    }
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    if (getWhereStr() == false)
      return;
    wp.pageControl();

    wp.selectSQL = ""
        // + "a.mod_date, "
        + "a.mod_time, " + "a.mod_user, " + "a.card_no, " + "a.return_date, " + "a.reason_code, "
        + "a.mail_type, " + "a.zip_code, " + "a.mail_addr1, " + "a.mail_addr2, " + "a.mail_addr3, "
        + "a.mail_addr4, " + "a.mail_addr5, "
        + "(a.zip_code||a.mail_addr1||a.mail_addr2||a.mail_addr3||a.mail_addr4||a.mail_addr5) db_addr, "
        + "a.proc_status, " + "a.id_p_seqno, " + "b.id_no, " + "b.id_no_code, "
        + "( b.id_no||'_'||b.id_no_code) db_id_no, " + "b.chi_name, " + "a.return_note, "
        + "a.acct_no," + "decode(a.digital_flag,'','N',a.digital_flag) as digital_flag ,a.return_seqno "
        + ",a.barcode_num ";
        
    wp.daoTable = " dbc_return a left join dbc_idno b on  a.id_p_seqno = b.id_p_seqno ";
    wp.whereOrder = " order by a.card_no";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();

    wp.colSet("query_log", null);
  }

  public void dataRead1() throws Exception {

    getWhereStr();

    wp.pageControl();
    wp.selectSQL = "a.mod_date, " + "a.mod_time, " + "a.mod_user, " + "a.card_no, "
        + "a.return_date, " + "a.reason_code, " + "a.mail_type, " + "a.zip_code, "
        + "a.mail_addr1, " + "a.mail_addr2, " + "a.mail_addr3, " + "a.mail_addr4, "
        + "a.mail_addr5, "
        + "(a.zip_code||a.mail_addr1||a.mail_addr2||a.mail_addr3||a.mail_addr4||a.mail_addr5) db_addr, "
        + "a.proc_status, " + "a.id_p_seqno, " + "b.id_no, " + "b.id_no_code, "
        + "( b.id_no||'_'||b.id_no_code) db_id_no, " + "b.chi_name, " + "a.return_note, "
        + "a.acct_no," + "decode(a.digital_flag,'','N',a.digital_flag) as digital_flag ,a.return_seqno ,a.barcode_num";
    wp.daoTable = " dbc_return_log a left join dbc_idno b on  a.id_p_seqno = b.id_p_seqno ";
    wp.whereOrder = " order by a.mod_date desc, a.mod_time desc ";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();

    wp.colSet("query_log", "true");
  }

  void listWkdata() throws Exception {
    int row_ct = 0;
    getMsg();
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      row_ct += 1;
      wp.colSet(ii, "group_ct", "1");

      // db_reason_code郵局退回原因
      String reasonCode = wp.colStr(ii, "reason_code");;
      String[] cde = new String[] {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10",};
      String[] txt = new String[] {"查無此公司", "查無此人", "遷移不明", "地址欠詳", "查無此址", "收件人拒收", "招領逾期", "分行退件",
          "其他", "地址改變"};
      wp.colSet(ii, "db_reason_code", commString.decode(reasonCode, cde, txt));

      // db_mail_type寄件別
      String mailType = wp.colStr(ii, "mail_type");;
      String[] cde1 = arrayMsgValue;
      String[] txt1 = arrayMsg;
      wp.colSet(ii, "db_mail_type", commString.decode(mailType, cde1, txt1));

      // db_proc_status處理結果
      String procStatus = wp.colStr(ii, "proc_status");;
      String[] cde2 = new String[] {"1", "2", "3", "4", "5", "6", "7"};
      String[] txt2 = new String[] {"1.處理中", "2.銷毀", "3.寄出", "4.申停", "5.重製", "6.寄出不封裝", "7.庫存"};
      wp.colSet(ii, "db_proc_status", commString.decode(procStatus, cde2, txt2));

    }
    wp.colSet("row_ct", intToStr(row_ct));
  }

  @Override
  public void querySelect() throws Exception {
    exCardNo = wp.itemStr("card_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    exCardNo = wp.itemStr("kk_card_no");
    exReturnDate = itemKk("data_k2");
    if (empty(exCardNo)) {
      exCardNo = itemKk("data_k1");
    }

    wp.selectSQL = "hex(a.rowid) as rowid, a.mod_seqno, " + "a.card_no, " + "a.return_date, "
        + "a.group_code, " + "a.id_p_seqno, " + "a.ic_flag, " + "a.reason_code, "
        + "a.return_type, " + "a.mail_type, " + "a.mail_branch, " + "a.zip_code, "
        + "a.mail_addr1, " + "a.mail_addr2, " + "a.mail_addr3, " + "a.mail_addr4, "
        + "a.mail_addr5, "
        + "(a.zip_code||a.mail_addr1||a.mail_addr2||a.mail_addr3||a.mail_addr4||a.mail_addr5) as db_addr, "
        + "a.proc_status, " + "a.package_flag, " + "a.package_date, " + "a.return_note, "
        + "a.mod_user, " + "a.mod_time, " + "a.mod_pgm, " + "a.mod_seqno, " + "b.id_no, "
        + "b.id_no_code, " + "( b.id_no||'_'||b.id_no_code) as db_id_no, " + "b.chi_name, "
        + "a.beg_date, " + "a.end_date, " + "' ' db_subflag, " + "LEFT(a.mail_no,6) as mail_no, "
        + "a.return_date as db_return_date, " + "a.barcode_num, " + "a.mail_date as mail_date, "
        + "a.acct_no," + "a.return_seqno as return_seqno, "
        + "decode(a.digital_flag,'','N',a.digital_flag) as digital_flag ,a.mail_addr_flag ";
    wp.daoTable = "dbc_return a left join dbc_idno b on a.id_p_seqno = b.id_p_seqno ";
    wp.whereStr = "where 1=1";
    wp.whereStr += sqlCol(exCardNo, "a.card_no");
    wp.whereStr += sqlCol(exReturnDate, "return_date");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無退件資料, card_no=" + exCardNo);
    }

    // db_addr帳單地址
    exIdpSeqno = wp.itemStr("id_p_seqno");
    if (colIsEmpty("zip_code") && notEmpty(exIdpSeqno)) {
      String lsSql = "";
      lsSql = "select " + "bill_sending_addr1, " + "bill_sending_addr2, " + "bill_sending_addr3, "
          + "bill_sending_addr4, " + "bill_sending_addr5, " + "bill_sending_zip||"
          + "bill_sending_addr1||" + "bill_sending_addr2||" + "bill_sending_addr3||"
          + "bill_sending_addr4||" + "bill_sending_addr5 " + "as db_addr  " + "from dba_acno ";
      lsSql += "where 1=1 ";
      lsSql += sqlCol(exIdpSeqno, "id_p_seqno");
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        wp.colSet("db_addr", sqlStr("db_addr"));
        wp.colSet("mail_addr1", sqlStr("bill_sending_addr1"));
        wp.colSet("mail_addr2", sqlStr("bill_sending_addr2"));
        wp.colSet("mail_addr3", sqlStr("bill_sending_addr3"));
        wp.colSet("mail_addr4", sqlStr("bill_sending_addr4"));
        wp.colSet("mail_addr5", sqlStr("bill_sending_addr5"));

      }
    }

    //分行地址
    getChiAddr("");
    
    // db_group_code
    String lsSql2 = "";
    String dbGroupCode = "";
    dbGroupCode = wp.colStr("group_code");
    lsSql2 = "select group_code,group_name from ptr_group_code ";
    lsSql2 += "where 1=1 ";
    lsSql2 += sqlCol(dbGroupCode, "group_code");

    sqlSelect(lsSql2);
    if (sqlRowNum > 0) {
      wp.colSet("db_group_code", dbGroupCode + "[" + sqlStr("group_name") + "]");
    }

  }

  @Override
  public void saveFunc() throws Exception {
    Dbcm0160Func func = new Dbcm0160Func(wp);
    String lsSql = "", return_seqno = "";

    // 若PK(card_no + return_date)已存在，則不允許新增此筆資料。
    // 若不存在，則繼續使用A(新增)。
    if (strAction.equals("A")) {
      if (empty(wp.itemStr("return_date"))) {
        alertErr2("請輸入退卡日期。");
        return;
      }

      lsSql =
          "select count(*) as tot_cnt from dbc_return where card_no =:card_no and return_date=:return_date ";
      setString("card_no", wp.itemStr("card_no"));
      setString("return_date", wp.itemStr("return_date"));
      sqlSelect(lsSql);

      if (sqlNum("tot_cnt") > 0) {
        alertErr2("此卡號及退卡日期已存在。若要異動資料，請使用修改。");
        return;
      }

      // 產出退卡編號
      return_seqno = generateReturnSeqno();
      wp.colSet("return_seqno", return_seqno);
    }

    // 執行新增或更新
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
      // 如果rowid是非空，則代表是從主畫面進入，因此不允許資料讀取。
      if (!empty(wp.colStr("rowid"))) {
        wp.colSet("btnQuery_disable", "disabled");

      }
    }
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "";
      wp.optionKey = wp.colStr("mail_branch");
      this.dddwList("dddw_branch", "gen_brn", "branch", "brief_chi_name",
          "where 1=1  order by branch");

      wp.initOption = "";
      wp.optionKey = wp.colStr("zip_code");
      this.dddwList("dddw_zipcode", "ptr_zipcode", "zip_code", "", "where 1=1  order by zip_code");

      
      wp.optionKey = wp.colStr("mail_type");
      this.dddwList("dddw_mail_type", "crd_message", "msg_value", "msg",
              "where 1=1 and msg_type = 'MAIL_TYPE' order by msg_value");
      
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("group_code");
      // this.dddw_list("dddw_group_code", "ptr_group_code", "group_code", "group_name", "where 1=1
      // order by group_code");
    } catch (Exception ex) {
    }
  }

  // 找出資料庫中今年最大的退卡編號，並回傳此退卡編號+1。
  // 若無找到，則自動產生今年第一筆流水號。
  // 如使用新增功能時是2019年，第一筆退卡編號為201900001。
  private String generateReturnSeqno() {
    String lsSql = "";
    String thisYear, maxNo, returnSeqno;
    thisYear = getSysDate().substring(0, 4);

    // 找尋今年最大的return_seqno
    lsSql = "select max(return_seqno) max_no from dbc_return " + "where return_seqno like ? ";
    sqlSelect(lsSql, new Object[] {thisYear+"%"});
    maxNo = sqlStr("max_no");
    if (!empty(maxNo)) {
      returnSeqno = Integer.toString(Integer.parseInt(maxNo) + 1);
    } else {
      returnSeqno = thisYear + "00001";
    }
    return returnSeqno;
  }

  void itemchanged() throws Exception {
    String paramString;
    String col = "";
    switch (wp.itemStr("cond")) {
      case "1":
        col = "card_no";
        break;
      case "2":
        col = "mail_no";
        break;
      case "3":
        col = "barcode_num";
        break;
    }
    paramString = wp.itemStr("kk_cond_val");
    if (wfColNameData(col, paramString) != 1) {
      alertErr2(lsMsg);
      return;
    }

  }

  int wfColNameData(String colName, String as_cardno) throws Exception {
    String lsSql = "", lsGroudCode = "";
    String lsIcFlag = "";
    // select by colName
    lsSql = "select id_p_seqno," + "p_seqno," + "uf_idno_name(id_p_seqno) as chi_name,"
        + "uf_idno_id(id_p_seqno) as id_no,"
        + "decode(digital_flag,'','N',digital_flag) as digital_flag, " + "acct_no, "
        + "new_beg_date," + "new_end_date," + "group_code," + "ic_flag," + "mail_type,"
        + "mail_branch, " + "LEFT(mail_no,6) as mail_no," + "barcode_num, " + "card_no " + "from dbc_card " + "where "
        + colName + " = :colValue";
    setString("colValue", as_cardno);
    sqlSelect(lsSql);
    if (sqlRowNum <= 0) {
      String[] cde = new String[] {"card_no", "mail_no", "barcode_num"};
      String[] txt = new String[] {"卡號", "掛號號碼", "掛號條碼"};
      String colChineseName = commString.decode(colName, cde, txt);
      lsMsg = colChineseName + "不存在,請重新輸入!!";
      return -1;
    } else {
      idSeqno = sqlStr("id_p_seqno");
      pSeqno = sqlStr("p_seqno");
      lsGroudCode = sqlStr("group_code");
      wp.colSet("id_p_seqno", idSeqno);
      wp.colSet("acct_no", sqlStr("acct_no"));
      wp.colSet("chi_name", sqlStr("chi_name"));
      wp.colSet("db_id_no", sqlStr("id_no"));
      wp.colSet("digital_flag", sqlStr("digital_flag"));
      wp.colSet("beg_date", sqlStr("new_beg_date"));
      wp.colSet("end_date", sqlStr("new_end_date"));
      lsIcFlag = sqlStr("ic_flag");
      if (empty(lsIcFlag))
        lsIcFlag = "N";
      wp.colSet("ic_flag", lsIcFlag);
      wp.colSet("mail_type", sqlStr("mail_type"));
      wp.colSet("mail_branch", sqlStr("mail_branch"));
      wp.colSet("group_code", sqlStr("group_code"));
      wp.colSet("package_flag", "N");
      wp.colSet("mail_no", sqlStr("mail_no"));
      wp.colSet("barcode_num", sqlStr("barcode_num"));
      wp.colSet("card_no", sqlStr("card_no"));
      wp.colSet("return_seqno", sqlStr("return_seqno"));
      // wp.col_set("cond", wp.item_ss("cond"));
      wp.colSet("kk_cond_val", as_cardno);

    }
    // db_group_code
    String lsSql2 = "";
    lsSql2 = "select group_code, group_name from ptr_group_code ";
    lsSql2 += "where 1=1 ";
    lsSql2 += sqlCol(lsGroudCode, "group_code");
    sqlSelect(lsSql2);
    if (sqlRowNum > 0) {
      wp.colSet("db_group_code", lsGroudCode + "[" + sqlStr("group_name") + "]");
    }

    // select act_acno
    lsSql = "select bill_sending_zip||" + "bill_sending_addr1||" + "bill_sending_addr2||"
        + "bill_sending_addr3||" + "bill_sending_addr4||" + "bill_sending_addr5 " + "as db_addr "
        + "from dba_acno " + "where p_seqno =:ls_p_seqno";
    setString("ls_p_seqno", pSeqno);
    sqlSelect(lsSql);
    if (sqlRowNum <= 0) {
      lsMsg = "查無帳號資料!!";
      return -1;
    } else {
      wp.colSet("db_addr", sqlStr("db_addr"));
    }

    return 1;
  }
  
  void getMsg() {
	  String sqlMsg = "select msg_value,msg_value||'.'||msg as db_msg from crd_message where msg_type = 'MAIL_TYPE' ";
	  sqlSelect(sqlMsg);
	  arrayMsgValue = new String[sqlRowNum]; 
	  arrayMsg = new String[sqlRowNum]; 
	  for(int x = 0; x<sqlRowNum; x++) {
		  arrayMsgValue[x] = sqlStr(x,"msg_value");
		  arrayMsg[x] = sqlStr(x,"db_msg");
	  }
  }
  
	void getChiAddr(String parameter) throws Exception {
		if(parameter.equals("AJAX")&&!wp.itemEmpty("id_no_json")) {
			if(!wp.itemEmpty("mail_addr_flag_json")&&wp.itemEmpty("mail_branch_json")) {
				String idNoJson = wp.itemStr("id_no_json");
				String mailAddrFlagJson = wp.itemStr("mail_addr_flag_json");
				String sqlCmd = "select ";
				switch(mailAddrFlagJson) {
				case"1":
					sqlCmd += " RESIDENT_ZIP as ZIP ,RESIDENT_ADDR1 as ADDR1,RESIDENT_ADDR2 as ADDR2,RESIDENT_ADDR3 as ADDR3,RESIDENT_ADDR4 as ADDR4,RESIDENT_ADDR5 as ADDR5 ";
					break;
				case"2":
					sqlCmd += " MAIL_ZIP as ZIP ,MAIL_ADDR1 as ADDR1,MAIL_ADDR2 as ADDR2,MAIL_ADDR3 as ADDR3,MAIL_ADDR4 as ADDR4,MAIL_ADDR5 as ADDR5 ";
					break;
				case"3":
					sqlCmd += " COMPANY_ZIP as ZIP ,COMPANY_ADDR1 as ADDR1,COMPANY_ADDR2 as ADDR2,COMPANY_ADDR3 as ADDR3,COMPANY_ADDR4 as ADDR4,COMPANY_ADDR5 as ADDR5 ";
					break;
				}
				sqlCmd += " from dbc_idno where id_no = :id_no_json ";
				setString("id_no_json",idNoJson);
				sqlSelect(sqlCmd);
				wp.addJSON("zip_code_json", sqlStr("ZIP"));
				wp.addJSON("mail_addr1_json", sqlStr("ADDR1"));
				wp.addJSON("mail_addr2_json", sqlStr("ADDR2"));
				wp.addJSON("mail_addr3_json", sqlStr("ADDR3"));
				wp.addJSON("mail_addr4_json", sqlStr("ADDR4"));
				wp.addJSON("mail_addr5_json", sqlStr("ADDR5"));
				wp.addJSON("flag_json", "Y");
			}
			return;
		}

		String lsSql = "select g.chi_addr_1,g.chi_addr_2,g.chi_addr_3,g.chi_addr_4,g.chi_addr_5, ";
		lsSql += " p.zip_code ";
		lsSql += " from gen_brn g left join ptr_zipcode p ";
		lsSql += " on g.chi_addr_1 = p.zip_city ";
		lsSql += " and g.chi_addr_2 = p.zip_town ";
		lsSql += " where 1=1 ";
		lsSql += sqlCol(wp.colStr("mail_branch"), "g.branch");
		lsSql += " fetch first 1 rows only ";
		sqlSelect(lsSql);
		if(parameter.equals("AJAX")) {
			wp.addJSON("ajax_zip_code", sqlStr("zip_code"));
			wp.addJSON("ajax_chi_addr_1", sqlStr("chi_addr_1"));
			wp.addJSON("ajax_chi_addr_2", sqlStr("chi_addr_2"));
			wp.addJSON("ajax_chi_addr_3", sqlStr("chi_addr_3"));
			wp.addJSON("ajax_chi_addr_4", sqlStr("chi_addr_4"));
			wp.addJSON("ajax_chi_addr_5", sqlStr("chi_addr_5"));
			wp.addJSON("flag_ajax", "Y");
		}else {
			wp.colSet("db_zip_code", sqlStr("zip_code"));
			wp.colSet("db_mail_addr1", sqlStr("chi_addr_1"));
			wp.colSet("db_mail_addr2", sqlStr("chi_addr_2"));
			wp.colSet("db_mail_addr3", sqlStr("chi_addr_3"));
			wp.colSet("db_mail_addr4", sqlStr("chi_addr_4"));
			wp.colSet("db_mail_addr5", sqlStr("chi_addr_5"));
		}
	}
	
	//整批匯入
	public void procFunc() throws Exception {
		if (itemIsempty("zz_file_name")) {
			alertErr2("上傳檔名: 不可空白");
			return;
		}
		fileDataImp();
	}
	
	void fileDataImp() throws Exception {
		TarokoFileAccess tf = new TarokoFileAccess(wp);

		String inputFile = wp.itemStr("zz_file_name");

		int fi = tf.openInputText(inputFile, "MS950");
		if (fi == -1) return;

		int llOk = 0, llCnt = 0, errCnt = 0;
		int returnSeqno = toInt(generateReturnSeqno());
		while (true) {
			String line = tf.readTextFile(fi);
			if (tf.endFile[fi].equals("Y")) break;

			llCnt ++;
			
			// split columns
			boolean isValidFormat = false;
			if (line.indexOf(",") != -1) {
				String batchBarcodeNum = "";
				String[] strArr = line.split(",");
				if (strArr.length == 1) {
					batchBarcodeNum = strArr[0].trim();
					isValidFormat = true;
					
					if (empty(batchBarcodeNum)) {
						setProcessResult(llCnt, batchBarcodeNum, "輸入掛號條碼不可為空");
						errCnt ++;
						continue;
					}
					if (batchBarcodeNum.length()>20) {
						setProcessResult(llCnt, batchBarcodeNum, "輸入掛號條碼長度不可大於20碼");
						errCnt ++;
						continue;
					}
					
		
					if(wfColNameData("barcode_num", batchBarcodeNum) == -1) {
						setProcessResult(llCnt, batchBarcodeNum, lsMsg);
						errCnt ++;
						continue;
					}
					
					if(inserrtDbcReturn(batchBarcodeNum,returnSeqno) == -1) {
						setProcessResult(llCnt, batchBarcodeNum, lsMsg);
						errCnt ++;
						continue;
					}
					
					setProcessResult(llCnt, batchBarcodeNum, "");
					llOk++;
					returnSeqno++;
				}	
			}

			if (isValidFormat == false) {
				setProcessResult(llCnt, "", String.format("資料格式不符[%s]", line));
				errCnt ++;
				continue;	
			}

		}
		
		wp.selectCnt = llCnt;
		wp.setListCount(1);
		
		String finalResult = String.format("資料匯入處理筆數[%d], 成功筆數[%d], 錯誤筆數[%d] ", llCnt, llOk, errCnt);
		wp.showLogMessage("I", "", finalResult);
		wp.alertMesg(finalResult);
		tf.closeInputText(fi);
		wp.colSet("zz_file_name", "");

	}
	
	private void setProcessResult(int llCnt, String batchBarcodeNum, String batchErrorMsg) {
		wp.colSet(llCnt-1, "batch_ser_num", llCnt);
		wp.colSet(llCnt-1, "batch_barcode_num", batchBarcodeNum);
		wp.colSet(llCnt-1, "batch_error_msg", batchErrorMsg);
	}
	
	private int inserrtDbcReturn(String batchBarcodeNum,int returnSeqno) {
		String sqlSelect = "select count(*) as dbc_return_cnt from dbc_return where card_no = :card_no and return_date = :return_date ";
		setString("card_no",wp.colStr("card_no"));
		setString("return_date",wp.sysDate);
		sqlSelect(sqlSelect);
		int dbcReturnCnt = sqlInt("dbc_return_cnt");
		if(dbcReturnCnt > 0) {
			lsMsg = String.format("dbc_return 資料已存在 ,card_no = [%s] ,return_date = [%s]", wp.colStr("card_no"),wp.sysDate);
			return -1;
		}
		
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("dbc_return");
		sp.ppstr("card_no", wp.colStr("card_no"));
		sp.ppstr("acct_no", wp.colStr("acct_no"));
		sp.ppstr("id_p_seqno", idSeqno);
		sp.ppstr("ic_flag", wp.colStr("ic_flag"));
		sp.ppstr("beg_date", wp.colStr("beg_date"));
		sp.ppstr("barcode_num", batchBarcodeNum);
		sp.ppstr("end_date", wp.colStr("end_date"));
		sp.ppstr("return_date", wp.sysDate);
		sp.ppstr("return_type", "");
		sp.ppstr("reason_code", "");
		sp.ppstr("group_code", wp.colStr("group_code"));
		sp.ppstr("mail_type", "");
		sp.ppstr("mail_branch", wp.colStr("mail_branch"));
		sp.ppstr("mail_no", wp.colStr("mail_no"));
	    sp.ppstr("zip_code", "");
	    sp.ppstr("mail_addr1", "");
	    sp.ppstr("mail_addr2", "");
	    sp.ppstr("mail_addr3", "");
	    sp.ppstr("mail_addr4", "");
	    sp.ppstr("mail_addr5", "");
		sp.ppstr("proc_status", "1");
		sp.ppstr("return_note", "");
		sp.ppint("return_seqno", returnSeqno);
		sp.ppstr("package_flag", wp.colStr("package_flag"));
		sp.ppstr("package_date", "");
		sp.addsql(", mod_time ", ", sysdate ");
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.ppnum("mod_seqno", 1);

		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			lsMsg = "inserrt dbc_return error";
			return -1;
		}
		
		sp.sql2Insert("dbc_return_log");
		sp.ppstr("card_no", wp.colStr("card_no"));
		sp.ppstr("acct_no", wp.colStr("acct_no"));
		sp.ppstr("id_p_seqno", idSeqno);
		sp.ppstr("ic_flag", wp.colStr("ic_flag"));
		sp.ppstr("beg_date", wp.colStr("beg_date"));
		sp.ppstr("barcode_num", batchBarcodeNum);
		sp.ppstr("end_date", wp.colStr("end_date"));
		sp.ppstr("return_date", wp.sysDate);
		sp.ppstr("return_type", "");
		sp.ppstr("reason_code", "");
		sp.ppstr("group_code", wp.colStr("group_code"));
		sp.ppstr("mail_type", wp.colStr("mail_type"));
		sp.ppstr("mail_branch", wp.colStr("mail_branch"));
		sp.ppstr("mail_no", wp.colStr("mail_no"));
		sp.ppstr("zip_code", "");
		sp.ppstr("mail_addr1", "");
		sp.ppstr("mail_addr2", "");
		sp.ppstr("mail_addr3", "");
		sp.ppstr("mail_addr4", "");
		sp.ppstr("mail_addr5", "");
		sp.ppstr("proc_status", "1");
		sp.ppstr("return_note", "");
		sp.ppstr("return_seqno", wp.colStr("return_seqno"));
		sp.ppstr("package_flag", wp.colStr("package_flag"));
		sp.ppstr("package_date", "");
		sp.addsql(", mod_time ", ", sysdate ");
		sp.addsql(", mod_date ", ", to_char(sysdate,'yyyymmdd') ");
		sp.ppstr("mod_user", wp.loginUser);

		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			lsMsg = "inserrt dbc_return_log error";
			return -1;
		}
		return 1;
	}
}
