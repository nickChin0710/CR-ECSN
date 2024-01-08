package cmsr02;
/** 19-0614:   JH    p_xxx >>acno_p_xxx
 109-04-28  shiyuqi       updated for project coding standard     * 
 * 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
 * */
import ofcapp.BaseAction;
import taroko.com.TarokoFileAccess;

public class Cmsr0080 extends BaseAction {

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
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    String lsDateS = "", lsDateE = "";

    lsDateS = wp.itemStr("ex_sdate");
    lsDateE = wp.itemStr("ex_edate");

    if (chkStrend(lsDateS, lsDateE) == false) {
      alertErr2("產生日期 起迄錯誤");
      return;
    }

    String lsWhere = " where 1=1 " + sqlStrend(lsDateS, lsDateE, "crt_date")
        + sqlCol(wp.itemStr("ex_batch_no"), "batch_no")
        + sqlCol(wp.itemStr("ex_remark"), "crt_desc");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL =
        " crt_date , " + " crt_user , " + " batch_no , " + " crt_desc , " + " count(*) as db_cnt ";
    wp.daoTable = "ecs_addr_label";
    wp.whereOrder = " group by crt_date , crt_user , batch_no , crt_desc order by crt_date Asc ";

    wp.pageCountSql =
        " select count(*) from (select distinct crt_date , crt_user , batch_no , crt_desc from ecs_addr_label "
            + wp.whereStr + " )";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setPageValue();
    wp.setListCount(0);

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

    if (wp.itemEmpty("ex_remark")) {
      alertErr2("匯入說明: 不可空白");
      return;
    }

    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
    }

    fileDataImp();

  }

  void fileDataImp() throws Exception {
    TarokoFileAccess tf = new TarokoFileAccess(wp);

    // String inputFile = wp.dataRoot + "/upload/" + wp.col_ss("file_name");
    // String inputFile = wp.dataRoot + "/upload/" + wp.item_ss("file_name");
    String inputFile = wp.itemStr("zz_file_name");
    // int fi = tf.openInputText(inputFile,"UTF-8");
    int fi = tf.openInputText(inputFile, "MS950");
    if (fi == -1) {
      return;
    }

    busi.ecs.EcsAddrLabel func = new busi.ecs.EcsAddrLabel();
    func.setConn(wp);

    wp.itemSet("imp_file_name", wp.colStr("zz_file_name"));



    String[] tt = new String[2];
    int llOk = 0, llCnt = 0, llErr = 0, llErrX = -1;
    while (true) {
      String file = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y")) {
        break;
      }
      if (file.length() < 2) {
        continue;
      }
      func.clear();
      llCnt++;
      tt[0] = file;
      // id_no, card_no, bill_zip, bill_addr1, bill_addr2, chi_name,
      // memo1, memo2, memo3, memo4
      tt = commString.token(tt, ",");
      func.idNo = tt[1];
      tt = commString.token(tt, ",");
      func.cardNo = tt[1];
      tt = commString.token(tt, ",");
      func.addrZip = tt[1];
      tt = commString.token(tt, ",");
      func.addr14 = tt[1];
      tt = commString.token(tt, ",");
      func.addr5 = tt[1];
      tt = commString.token(tt, ",");
      func.chiName = tt[1];
      tt = commString.token(tt, ",");
      func.remark1 = tt[1];
      tt = commString.token(tt, ",");
      func.remark2 = tt[1];
      tt = commString.token(tt, ",");
      func.remark3 = tt[1];
      tt = commString.token(tt, ",");
      func.remark4 = tt[1];
      func.batchNo = wp.itemStr("ex_batch_no");
      func.crtDesc = wp.itemStr("ex_remark");
      func.idnoSex = "先生/小姐";
      func.crtPgm = "cmsr0080";

      if (empty(func.addrZip) || empty(func.addr14) || empty(func.addr5) || empty(func.chiName)) {
        llErr++;
        llErrX++;
        if (llErr < 10) {
          wp.colSet(llErrX, "ser_num", "0" + llErr);
        } else
          wp.colSet(llErrX, "ser_num", "" + llErr);
        wp.colSet(llErrX, "id_no", func.idNo);
        wp.colSet(llErrX, "card_no", func.cardNo);
        wp.colSet(llErrX, "bill_zip", func.addrZip);
        wp.colSet(llErrX, "bill_addr14", func.addr14);
        wp.colSet(llErrX, "bill_addr5", func.addr5);
        wp.colSet(llErrX, "chi_name", func.chiName);
        wp.colSet(llErrX, "memo1", func.remark1);
        wp.colSet(llErrX, "memo2", func.remark2);
        wp.colSet(llErrX, "memo3", func.remark3);
        wp.colSet(llErrX, "memo4", func.remark4);
        wp.colSet(llErrX, "errmsg", "郵遞區號 , 住址 , 姓名 不可空白 !");
        continue;
      }

      if (func.dataInsert() == 1) {
        llOk++;
        sqlCommit(1);
        continue;
      } else {
        sqlCommit(-1);
        llErr++;
        llErrX++;
        if (llErr < 10) {
          wp.colSet(llErrX, "ser_num", "0" + llErr);
        } else
          wp.colSet(llErrX, "ser_num", "" + llErr);
        wp.colSet(llErrX, "id_no", func.idNo);
        wp.colSet(llErrX, "card_no", func.cardNo);
        wp.colSet(llErrX, "bill_zip", func.addrZip);
        wp.colSet(llErrX, "bill_addr14", func.addr14);
        wp.colSet(llErrX, "bill_addr5", func.addr5);
        wp.colSet(llErrX, "chi_name", func.chiName);
        wp.colSet(llErrX, "memo1", func.remark1);
        wp.colSet(llErrX, "memo2", func.remark2);
        wp.colSet(llErrX, "memo3", func.remark3);
        wp.colSet(llErrX, "memo4", func.remark4);
        wp.colSet(llErrX, "errmsg", func.getMsg());
        continue;
      }

    }

    tf.closeInputText(fi);
    tf.deleteFile(inputFile);

    alertMsg("存檔完成; OK=" + llOk + ", ERR=" + llErr);
    wp.listCount[0] = llErrX + 1;
    return;
  }

  public String selectIdpseqno(String lsIdNo) {
    String sql1 = " select ";
    return sqlStr("id_p_seqno");
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
