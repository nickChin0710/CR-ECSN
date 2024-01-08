/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-03-10  V1.00.00  YangHan   program initial                             *
******************************************************************************/
package crdr01;

import java.io.InputStream;
import java.util.Calendar;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Crdr0017 extends BaseReport {
  CommString commString = new CommString();

  // String m_progName = "crdr0017";
  // String reportSubtitle ="";
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
      // /* 瀏覽功能 :skip-page */
      // queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      // /* 動態查詢 */
      // querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      // wp.setExcelMode();
      // xlsPrint();
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

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1 ";

    if (!empty(wp.itemStr("ex_reg_bank_no"))) {
      wp.whereStr += "and crd_emboss.reg_bank_no = :reg_bank_no ";
      setString("reg_bank_no", wp.itemStr("ex_reg_bank_no"));
    }

    if (eqIgno(wp.itemStr("ex_combo_indicator"), "0")) {
      wp.whereStr += "and crd_emboss.combo_indicator = 'N' ";
    } else if (eqIgno(wp.itemStr("ex_combo_indicator"), "1")) {
      wp.whereStr += "and crd_emboss.combo_indicator <> 'N' ";
    }

    if (!empty(wp.itemStr("ex_to_nccc_date"))) {
      wp.whereStr += "and crd_emboss.to_nccc_date = :to_nccc_date ";
      setString("to_nccc_date", wp.itemStr("ex_to_nccc_date"));
    }

    if (!empty(wp.itemStr("ex_emboss_source"))) {
      wp.whereStr += "and crd_emboss.emboss_source = :emboss_source ";
      setString("emboss_source", wp.itemStr("ex_emboss_source"));
    }
    // 查詢員工
    if (eqIgno(wp.itemStr("ex_id"), "Y")) {
      wp.whereStr += "and crd_employee.id=crd_emboss.apply_id ";
    } // 查詢非員工 此時查詢不需要crd_employee中的數據，故而
    else if (eqIgno(wp.itemStr("ex_combo_indicator"), "N")) {
      wp.whereStr +=
          "and  crd_emboss.apply_id in (select apply_id from crd_emboss where apply_id not in"
              + "(select crd_employee.id from crd_employee)) ";
    }
    wp.whereStr += "and crd_emboss.in_main_date <>'' and " + "crd_emboss.reject_code = '' and "
        + "crd_emboss.in_main_error = '0'";
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "crd_emboss.reg_bank_no, " + "crd_emboss.group_code, " + "crd_emboss.card_no, "
        + "crd_emboss.apply_id, " + "crd_emboss.chi_name , "// 5
        // + "crd_employee.chi_name as chi_name1, "
        + "crd_emboss.act_no, " + "crd_idno.home_area_code1 , " + "crd_idno.home_tel_no1 , "
        + "crd_idno.home_tel_ext1 , "// 10
        + "crd_idno.office_area_code1 , " + "crd_idno.office_tel_no1  , "
        + "crd_idno.office_tel_ext1 , " + "crd_idno.cellar_phone,  " + "crd_emboss.crt_bank_no, "
        + "crd_emboss.to_nccc_date ";
    wp.daoTable = " crd_emboss left join crd_idno on crd_emboss.apply_id=crd_idno.id_no";
    // 如果查询员工则需要crd_employee，若不查询，则不需要crd_employee
    if (eqIgno(wp.itemStr("ex_id"), "Y")) {
      wp.daoTable += ",crd_employee ";
    }
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    int count = 0;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算總卡數
      // count+= 1;
      // 處理公司電話 和家庭電話 因為有可能出現三個數據有兩個數據有值而另一個無值的情況，所以在此處加上“-”以示區分
      // 這樣使用者就知道缺少了那個數據，因此不再此處作判空操作
      String homePhone = wp.colStr(ii, "home_area_code1") + "-" + wp.colStr(ii, "home_tel_no1")
          + "-" + wp.colStr(ii, "home_tel_ext1");
      wp.colSet(ii, "home_phone", homePhone);
      String officePhone = wp.colStr(ii, "office_area_code1") + "-"
          + wp.colStr(ii, "office_tel_no1") + "-" + wp.colStr(ii, "office_tel_ext1");
      wp.colSet(ii, "office_phone", officePhone);

      // 判斷是否為員工 ，若是則查詢名字 並注入到員工姓名中 若不是則不注入
      if (eqIgno(wp.itemStr("ex_id"), "Y")) {
        String lsSql = "select chi_name as chi_name1 from crd_employee ";
        lsSql += " where id = :apply_id";
        setString("apply_id", wp.colStr(ii, "apply_id"));
        sqlSelect(lsSql);
        // System.out.println("到這了");
        wp.colSet(ii, "chi_name1", sqlStr("chi_name1"));
      } else if (eqIgno(wp.itemStr("ex_id"), "N")) {
        wp.colSet(ii, "chi_name1", "");
      }
    }
    // 總卡數
    wp.colSet("count", intToStr(wp.selectCnt));
    wp.listCount[1] = wp.dataCnt;
    wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
    wp.setPageValue();
  }

  void pdfPrint() throws Exception {
    wp.reportId = "crdr0017";
    Calendar now = Calendar.getInstance();
    String date = (now.get(Calendar.YEAR) - 1912 + 1) + "年" + (now.get(Calendar.MONTH) + 1) + "月"
        + now.get(Calendar.DAY_OF_MONTH) + "日";
    wp.colSet("cond_3", date);
    // System.out.println(date);
    // ===========================
    wp.pageRows = 99999;
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "crdr0017.xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 20;
    // pdf.pageVert= true; //直印
    pdf.procesPDFreport(wp);
    // System.out.println("到這了222");
    pdf = null;
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_reg_bank_no");
      this.dddwList("dddw_branch_name", "gen_brn", "branch", "full_chi_name",
          "where 1=1 order by branch");
    } catch (Exception ex) {
    }
  }
}

