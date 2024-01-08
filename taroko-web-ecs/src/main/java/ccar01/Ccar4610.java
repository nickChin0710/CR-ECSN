package ccar01;
/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*   DATE      Version   AUTHOR      DESCRIPTION                              *
*  109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard                           *
*  109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
*/
import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.base.CommString;
import taroko.com.TarokoPDF;

public class Ccar4610 extends BaseAction implements InfacePdf {

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


    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_user_id"), "user_id");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();


  }

  @Override
  public void queryRead() throws Exception {
    // wp.pageControl();

    wp.selectSQL = " user_id ," + " count(*) as db_cnt ," + " crt_date ," + " crt_time"

    ;
    wp.daoTable = "cca_passwd_list";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " group by user_id,crt_date,crt_time ";
    pageQuery();
    wp.setListCount(1);

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    // wp.setPageValue();


  }

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

    // if(check_user()==false){
    // alert_err("使用者: 不是授權覆核人員");
    // return;
    // }

    if (wp.itemNum("ex_passwd_cnt") < 20) {
      alertErr("警告訊息:請輸入大於20之數字 !");
      return;
    }

    Ccar4610Func func = new Ccar4610Func();
    func.setConn(wp);
    rc = func.dataProc();
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else {
      alertMsg("密碼產生完成");
    }


  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    wp.colSet("ex_user_id", wp.loginUser);

  }

  @Override
  public void pdfPrint() throws Exception {
    wp.reportId = "ccar4610";
    String cond1 = "使用者代碼:" + wp.itemStr("ex_user_id");
    wp.colSet("cond1", cond1);
    wp.colSet("user_id", wp.loginUser);
    wp.pageRows = 9999;
    printData();

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "ccar4610.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;

  }

  boolean checkUser() {

    String isSql = "select count(*) as db_cnt from cca_user_base" + " where user_id =? "
        + " and auth_flag='Y'";
    this.sqlSelect(isSql, new Object[] {wp.itemStr("ex_user_id")});
    if (sqlNum("db_cnt") > 0)
      return true;
    return false;
  }

  void printData() throws Exception {
    wp.sqlCmd = " select seq_no , chker_passwd from cca_passwd_list " + " where 1=1 "
        + sqlCol(wp.itemStr("ex_user_id"), "user_id") + " order by seq_no ";

    pageQuery();
    inData(wp.selectCnt);
    wp.setListCount(1);

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
  }

  void inData(int rr) {
    int cnt = 0;
    if (rr % 5 != 0) {
      cnt = (rr / 5) + 1;
    } else {
      cnt = (rr / 5);
    }
    wp.selectCnt = cnt;
    int seqNO = 0;
    for (int ii = 0; ii < cnt; ii++) {
      wp.colSet(ii, "seq_no1", wp.colStr(seqNO, "seq_no"));
      wp.colSet(ii, "chker_passwd1", wp.colStr(seqNO, "chker_passwd"));
      if (seqNO == rr)
        break;
      seqNO++;
      wp.colSet(ii, "seq_no2", wp.colStr(seqNO, "seq_no"));
      wp.colSet(ii, "chker_passwd2", wp.colStr(seqNO, "chker_passwd"));
      if (seqNO == rr)
        break;
      seqNO++;
      wp.colSet(ii, "seq_no3", wp.colStr(seqNO, "seq_no"));
      wp.colSet(ii, "chker_passwd3", wp.colStr(seqNO, "chker_passwd"));
      if (seqNO == rr)
        break;
      seqNO++;
      wp.colSet(ii, "seq_no4", wp.colStr(seqNO, "seq_no"));
      wp.colSet(ii, "chker_passwd4", wp.colStr(seqNO, "chker_passwd"));
      if (seqNO == rr)
        break;
      seqNO++;
      wp.colSet(ii, "seq_no5", wp.colStr(seqNO, "seq_no"));
      wp.colSet(ii, "chker_passwd5", wp.colStr(seqNO, "chker_passwd"));
      seqNO++;
      if (seqNO == rr)
        break;
    }

  }

}
