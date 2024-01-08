/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  shiyuqi       updated for project coding standard     *
* * 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package cmsm03;

import ofcapp.BaseAction;
import taroko.com.TarokoFileAccess;

public class Cmsm4220 extends BaseAction {

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
      // -異動處理-
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
    if (chkStrend(wp.itemStr2("ex_crt_date1"), wp.itemStr2("ex_crt_date2")) == false) {
      alertErr2("匯入日期：起迄輸入錯誤");
      return;
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr2("ex_crt_date1"), "crt_date", ">=")
        + sqlCol(wp.itemStr2("ex_crt_date2"), "crt_date", "<=");

    if (wp.itemEmpty("ex_idno") == false) {
      lsWhere += " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
          + sqlCol(wp.itemStr2("ex_idno"), "id_no") + ") ";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " uf_idno_id(id_p_seqno) as id_no , " + " item_no , "
        + " (select wf_id||'_'||wf_desc from ptr_sys_idtab where wf_type = 'RIGHT_ITEM_NO' and wf_id = item_no) as wk_item_no , "
        + " right_cnt , " + " right_date , " + " use_cnt , " + " last_use_date , "
        + " imp_file_name ";

    wp.daoTable = " cms_right_list ";
    wp.whereOrder = " order by 1 , right_date Asc ";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();

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
    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
    }

    fileDataImp();
  }

  void fileDataImp() throws Exception {
    int llErr = 0;
    TarokoFileAccess tf = new TarokoFileAccess(wp);

    // String inputFile = wp.dataRoot + "/upload/" + wp.col_ss("file_name");
    String inputFile = wp.itemStr("zz_file_name");
    // int fi = tf.openInputText(inputFile,"UTF-8");
    int fi = tf.openInputText(inputFile, "MS950");
    if (fi == -1) {
      return;
    }

    int fileErr = tf.openOutputText(inputFile + ".err", "UTF-8");

    cmsm03.Cmsm4220Func func = new cmsm03.Cmsm4220Func();
    func.setConn(wp);

    wp.itemSet("imp_file_name", wp.colStr("zz_file_name"));

    int llOK = 0, llCnt = 0;
    while (true) {
      String file = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y")) {
        break;
      }
      if (file.length() < 2) {
        continue;
      }
      // String merchantNo = readData.substring(0,15);
      // wp.ddd(ss);
      llCnt++;
      String[] tt = new String[2];
      tt[0] = file;
      tt = commString.token(tt, ",");
      String lsIdno = tt[1];
      String lsIdPSeqno = selectIdPseqno(lsIdno);

      if (empty(lsIdPSeqno)) {
        llErr++;
        tf.writeTextFile(fileErr, file + ";" + "此人非本行卡友" + wp.newLine);
        continue;
      }

      tt = commString.token(tt, ",");
      String lsItemNo = tt[1];

      if (checkItemNo(lsItemNo) == false) {
        llErr++;
        tf.writeTextFile(fileErr, file + ";" + "權益類別輸入錯誤" + wp.newLine);
        continue;
      }

      tt = commString.token(tt, ",");
      String lsRightCnt = tt[1];
      tt = commString.token(tt, ",");
      String lsRightDate = tt[1];

      wp.itemSet("id_p_seqno", lsIdPSeqno);
      wp.itemSet("item_no", lsItemNo);
      wp.itemSet("right_cnt", lsRightCnt);
      wp.itemSet("right_date", lsRightDate);

      if (func.insertRighList() == 1) {
        llOK++;
      } else {
        llErr++;
        tf.writeTextFile(fileErr, file + ";" + func.getMsg() + wp.newLine);
      }
    }

    if (llOK > 0) {
      sqlCommit(1);
    } else {
      sqlCommit(-1);
    }

    tf.closeOutputText(fileErr);
    tf.closeInputText(fi);
    tf.deleteFile(inputFile);

    if (llErr > 0) {
      wp.setDownload(inputFile + ".err");
    }

    alertMsg("資料匯入處理筆數: " + llCnt + ", 成功筆數=" + llOK);
    wp.colSet("zz_file_name", "");
    return;
  }

  String selectIdPseqno(String aIdno) {
    String sql1 = " select id_p_seqno from crd_idno where 1=1 and id_no = ? ";
    sqlSelect(sql1, new Object[] {aIdno});

    if (sqlRowNum > 0)
      return sqlStr("id_p_seqno");

    return "";

  }

  boolean checkItemNo(String aItemNo) {

    String sql1 =
        " select count(*) as db_cnt from ptr_sys_idtab where wf_type = 'RIGHT_ITEM_NO' and wf_id = ? ";
    sqlSelect(sql1, new Object[] {aItemNo});

    if (sqlRowNum < 0 || sqlNum("db_cnt") <= 0)
      return false;

    return true;
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
