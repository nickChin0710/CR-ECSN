/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE            Version     AUTHOR        DESCRIPTION                               *
* ---------       --------      ----------         ------------------------------------------ *
* 109-01-15  V1.00.00   Justin Wu        program initial                            *
* 109-01-16  V1.00.01   Justin Wu        PP卡 -> 貴賓卡
* 109-05-06  V1.00.02  shiyuqi      updated for project coding standard      *
 * 109-12-30  V1.00.03  shiyuqi       修改无意义命名                                                                                     *
* 112-05-30  V1.00.03  Ryan          PDF改EXCEL                                    
* 112-06-05  V1.00.04  Ryan          增加退卡編號、掛號條碼欄位,excel檔的卡號&姓名欄位的值不要隱碼                    
******************************************************************************/
package crdr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;


public class Crdr1530 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "crdr1530";

  String condWhere = "";
  String reportSubtitle = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml="
        + wp.respHtml);
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        // is_action="new";
        // clearFunc();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
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
      case "PDF":
        // -PDF-
        strAction = "PDF";
        // wp.setExcelMode();
        pdfPrint();
        break;
      case "XLS":
        // -PDF-
        strAction = "XLS";
        // wp.setExcelMode();
        xlsPrint();
        break;
      default:
        break;
    }

    // dddw_select();
    // init_button();
  }

  @Override
  public void clearFunc() throws Exception {
    wp.resetInputData();
    wp.resetOutputData();
  }

  @Override
  public void initPage() {

  }

  private boolean getWhereStr() throws Exception {
    String exOppostDateS = wp.itemStr("ex_oppost_date_s");
    String exOppostDateE = wp.itemStr("ex_oppost_date_e");
    String exPpCardNo = wp.itemStr("ex_pp_card_no");
    String exReturnDateS = wp.itemStr("ex_return_date_s");
    String exReturnDateE = wp.itemStr("ex_return_date_e");
    String exReturnType = wp.itemStr("ex_return_type");
    String exVipKind = wp.itemStr("ex_vip_kind");


    String lsWhere = "where 1=1  ";
    // 固定搜尋條件

    // user搜尋條件
    lsWhere += sqlStrend(exOppostDateS, exOppostDateE, "b.oppost_date");

    if (notEmpty(exPpCardNo)) {
      lsWhere += sqlCol(exPpCardNo, "a.pp_card_no");
    }

    lsWhere += sqlStrend(exReturnDateS, exReturnDateE, "a.return_date");

    if (!exReturnType.equals("0")) {
      lsWhere += sqlCol(exReturnType, "a.return_type");
    }

    if (!exVipKind.equals("0")) {
      lsWhere += sqlCol(exVipKind, "b.vip_kind");
    }

    lsWhere += sqlCol("ED", "b.oppost_reason");

    wp.whereStr = lsWhere;
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

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

//    if (getWhereStr() == false)
//      return;

    wp.selectSQL = "" + "b.vip_kind, " + "b.oppost_date, " + "a.pp_card_no, "
        + "uf_hi_cardno (a.pp_card_no) db_pp_card_no, "// 轉碼:貴賓卡號
        + "b.reissue_date, " + "b.change_date, " + "b.issue_date as db_issue_date, "
        + "a.id_p_seqno, " + "nvl(uf_idno_id(a.id_p_seqno),'') as id_no, "
        + "nvl(c.chi_name,'') as chi_name, " + "uf_hi_cname(nvl(c.chi_name,'')) as db_chi_name, "// 轉碼:中文姓名
        + "a.return_date, " + "a.return_type, " + "a.reason_code, " + "a.proc_status "
        + " ,a.return_seqno ,a.barcode_num ";
    wp.daoTable =
        " crd_return_pp as a " + " left join crd_card_pp as b on a.pp_card_no = b.pp_card_no "
            + " left join crd_idno as c on a.id_p_seqno = c.id_p_seqno ";
    wp.whereOrder = " order by a.return_date, a.pp_card_no ";

    pageQuery();
    // list_wkdata();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
    int rowCt = 0;
    int selCt = wp.selectCnt;
    for (int ii = 0; ii < selCt; ii++) {
      // 計算欄位
      rowCt += 1;
      wp.colSet(ii, "group_ct", "1");

      // issue_date 發卡日期
      switch (wp.colStr(ii, "return_type")) {
        case "3":
          wp.colSet(ii, "issue_date", wp.colStr(ii, "reissue_date"));
          break;
        case "2":
          wp.colSet(ii, "issue_date", wp.colStr(ii, "change_date"));
          break;
        default:
          wp.colSet(ii, "issue_date", wp.colStr(ii, "db_issue_date"));
          break;
      }

      // return_type 退卡性質中文
      String returnType = wp.colStr(ii, "return_type");
      String[] cde = new String[] {"1", "2", "3"};
      String[] txt = new String[] {"新製卡", "續卡", "毀損重製"};
      wp.colSet(ii, "return_type", commString.decode(returnType, cde, txt));

      // reason_code 退卡原因
      String reasonCode = wp.colStr(ii, "reason_code");
      String[] cde1 = new String[] {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10"};
      String[] txt1 = new String[] {"01.查無此公司", "02.查無此人", "03.遷移不明", "04.地址欠詳", "05.查無此址", "06.收件人拒收", "07.招領逾期 ",
              "08.分行退件", "09.其他", "10.地址改變" ,"11.先寄"};
      wp.colSet(ii, "reason_code", commString.decode(reasonCode, cde1, txt1));

      // proc_status 處理結果中文
      String procStatus = wp.colStr(ii, "proc_status");
      String[] cde2 = new String[] {"1", "2", "3", "4", "5", "6"};
      String[] txt2 = new String[] {"處理中", "銷毀", "寄出", "申停", "重製", "寄出不封裝"};
      wp.colSet(ii, "proc_status", commString.decode(procStatus, cde2, txt2));

      // vip_kind 處理結果中文
      String vipKind = wp.colStr(ii, "vip_kind");
      String[] cde3 = new String[] {"1", "2"};
      String[] txt3 = new String[] {"新貴通", "龍騰卡"};
      wp.colSet(ii, "vip_kind", commString.decode(vipKind, cde3, txt3));

    }
    wp.colSet("row_ct", intToStr(rowCt));
  }

  void subTitle() {
    String exOppostDateS = wp.itemStr("ex_oppost_date_s");
    String exOppostDateE = wp.itemStr("ex_oppost_date_e");
    String exPpCardNo = wp.itemStr("ex_pp_card_no");
    String exReturnDateS = wp.itemStr("ex_return_date_s");
    String exReturnDateE = wp.itemStr("ex_return_date_e");
    String exReturnType = wp.itemStr("ex_return_type");
    String exVipKind = wp.itemStr("ex_vip_kind");
    String vipKind = "";

    // 貴賓卡別
    String exVipKind1 = exVipKind;
    String[] cde1 = new String[] {"0", "1", "2"};
    String[] txt1 = new String[] {"全部", "新貴通", "龍騰卡"};
    vipKind += " 貴賓卡別: " + commString.decode(exVipKind1, cde1, txt1);

    // 停卡日期
    if (notEmpty(exOppostDateS) || notEmpty(exOppostDateE)) {
      vipKind += "退卡日期 : ";
      if (notEmpty(exOppostDateS)) {
        vipKind += exOppostDateS + " 起 ";
      }
      if (notEmpty(exOppostDateE)) {
        vipKind += " ~ " + exOppostDateE + " 迄 ";
      }
    }

    // 貴賓卡號
    if (notEmpty(exPpCardNo)) {
      vipKind += " 貴賓卡號 : " + exPpCardNo;
    }

    // 退卡日期
    if (notEmpty(exReturnDateS) || notEmpty(exReturnDateE)) {
      vipKind += "退卡日期 : ";
      if (notEmpty(exReturnDateS)) {
        vipKind += exReturnDateS + " 起 ";
      }
      if (notEmpty(exReturnDateE)) {
        vipKind += " ~ " + exReturnDateE + " 迄 ";
      }
    }

    // 退卡性質
    String returnType = exReturnType;
    String[] cde2 = new String[] {"0", "1", "2", "3"};
    String[] txt2 = new String[] {"全部", "新製卡", "續卡", "毀損重製"};
    vipKind += " 退卡性質: " + commString.decode(returnType, cde2, txt2);

    reportSubtitle = vipKind;
  }

  void pdfPrint() throws Exception {
    wp.reportId = mProgName;
    // -cond-
    subTitle();
    wp.colSet("cond_1", reportSubtitle);
    // ===========================
    wp.pageRows = 99999;
    queryFunc();

    // wp.setListCount(1);
    wp.colSet("user_id", wp.loginUser);
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 28;
    // pdf.pageVert= true; //直印
    pdf.procesPDFreport(wp);

    pdf = null;
  }
  
	void xlsPrint() {
		try {
			log("xlsFunction: started--------");
			wp.reportId = mProgName;
			// -cond-
			subTitle();
			wp.colSet("cond_1", reportSubtitle);
			wp.pageRows = 99999;
			// ===================================
			TarokoExcel xlsx = new TarokoExcel();
			wp.fileMode = "Y";
//			xlsx.pageBreak = "Y"; // 固定列數跳頁
//			xlsx.pageCount = 28; // 跳頁列數
			xlsx.excelTemplate = mProgName + ".xlsx";

			// ====================================
			// -明細-
			xlsx.sheetName[0] = "明細";
			queryFunc();
			wp.colSet("select_cnt", wp.selectCnt);
			wp.setListCount(1);
			log("Detl: rowcnt:" + wp.listCount[0]);
			xlsx.processExcelSheet(wp);
			xlsx.outputExcel();
			xlsx = null;
			log("xlsFunction: ended-------------");

		} catch (Exception ex) {
			wp.expMethod = "xlsPrint";
			wp.expHandle(ex);
		}
	}

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

}

