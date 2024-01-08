package cmsr03;
/** 
 * 19-1129:   Alex  fixed order by
 * 19-0617:   JH    p_xxx >>acno_p_xxx
 * 109-05-06  V1.00.00  Tanwei       updated for project coding standard 
 *  * 109-01-04  V1.00.03  shiyuqi       修改无意义命名                                                                                     *
 * */
import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Cmsr4500 extends BaseAction implements InfacePdf {

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
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {

    if (wp.itemEmpty("ex_date1") && wp.itemEmpty("ex_date2") && wp.itemEmpty("ex_idno")
        && wp.itemEmpty("ex_card_no") && wp.itemEmpty("ex_file_type")
        && wp.itemEmpty("ex_batch_no")) {
      alertErr2("篩選條件不可全部空白");
      return;
    }

    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("異動日期: 起迄錯誤");
      return;
    }

    String lsIdno = wp.itemStr2("ex_idno");
    if (!empty(lsIdno) && lsIdno.length() < 8) {
      alertErr2("身分證ID: 輸入錯誤, 不可少於8碼");
      return;
    }
    String lsCardNo = wp.itemStr2("ex_card_no");
    if (!empty(lsCardNo) && lsCardNo.length() < 6) {
      alertErr2("卡號: 輸入錯誤, 不可少於6碼");
      return;
    }

    String lsWhere = " where 1=1 " + sqlBetween("ex_date1", "ex_date2", "purchase_date")
        + sqlCol(wp.itemStr("ex_batch_no"), "batch_no", "like%")
        + sqlCol(wp.itemStr("ex_file_type"), "file_type");
    if (!empty(lsIdno)) {
      lsWhere += " and major_id_p_seqno in (select id_p_seqno from crd_idno where 1=1"
          + sqlCol(lsIdno, "id_no", "like%") + ")";
    } else if (wp.itemEmpty("ex_card_no") == false) {
      lsWhere += sqlCol(wp.itemStr2("ex_card_no"), "major_card_no", "like%");
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " crt_date , " + " purchase_date , " + " company_name , "
        + " uf_idno_id(major_id_p_seqno) as major_id_no , "
        + " uf_hi_idno(uf_idno_id(major_id_p_seqno)) as hh_idno , " + " major_card_no , "
        + " uf_hi_cardno(major_card_no) as hh_cardno , " + " batch_no , " + " car_no , "
        + " project_no , " + " service_name , " + " proc_flag " + ", decode(proc_flag,'0','請款未處理'"
        + ",'1','無此卡號','2','特店代號錯誤','3','商品代號錯誤'" + ",'4','消費日期錯誤','5','拖吊時已停用拖吊','Y','處理完成'"
        + ",proc_flag) as tt_proc_flag";
    wp.daoTable = "bil_mcht_apply_tmp";
    wp.whereOrder = " order by crt_date , proc_flag ";
    pageQuery();
    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    // queryAfter();
    wp.setListCount(0);
    wp.setPageValue();

  }

  // void queryAfter() {
  // for(int ii=0;ii<wp.selectCnt;ii++){
  // if(wp.col_eq(ii,"proc_flag", "0")){
  // wp.col_set(ii,"tt_proc_flag", "請款未處理");
  // } else if(wp.col_eq(ii,"proc_flag", "1")){
  // wp.col_set(ii,"tt_proc_flag", "無此卡號");
  // } else if(wp.col_eq(ii,"proc_flag", "2")){
  // wp.col_set(ii,"tt_proc_flag", "特店代號錯誤");
  // } else if(wp.col_eq(ii,"proc_flag", "3")){
  // wp.col_set(ii,"tt_proc_flag", "商品代號錯誤");
  // } else if(wp.col_eq(ii,"proc_flag", "4")){
  // wp.col_set(ii,"tt_proc_flag", "消費日期錯誤");
  // } else if(wp.col_eq(ii,"proc_flag", "5")){
  // wp.col_set(ii,"tt_proc_flag", "拖吊時已停用拖吊");
  // } else if(wp.col_eq(ii,"proc_flag", "Y")){
  // wp.col_set(ii,"tt_proc_flag", "處理完成");
  // }
  // }
  // }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
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

  @Override
  public void pdfPrint() throws Exception {
    wp.reportId = "cmsr4500";
    wp.pageRows = 9999;
    String cond1 = "";
    wp.colSet("cond1", cond1);
    wp.colSet("user_id", wp.loginUser);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "cmsr4500.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
