package colm05;
/**
 * 2019-1209   JH    dsp.T2
 * 109-05-06  V1.00.00  Tanwei       updated for project coding standard
 ** 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
 *
 * */
import ecsfunc.DeCodeRsk;
import ofcapp.AppMsg;
import ofcapp.BaseQuery;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colm5835 extends BaseQuery {
  String rowid = "";

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
    }

    dddwSelect();
    initButton();

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "colm5835")) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "ex_acct_type");
        dddwList("d_dddw_accttype", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
      }
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where param_type in ('2','4')" + sqlCol(wp.itemStr("ex_acct_type"), "acct_type")
        + sqlCol(wp.itemStr("ex_exec_date"), "exec_date", "like%");


    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "param_type, " + "acct_type," + "valid_date, " + "exec_date," + "exec_mode,"
        + "exec_times," + "exec_msg," + "hex(rowid) as rowid";
    wp.daoTable = "rsk_blockexec";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by exec_date Desc ";
    logSql();
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
  }

  void listWkdata() {
    String wkData = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, "exec_mode");
      wp.colSet(ii, "tt_exec_mode", DeCodeRsk.blockExecMode(wkData));
    }
  }

  @Override
  public void querySelect() throws Exception {
    rowid = wp.itemStr("data_k1");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "param_type,   " + "acct_type, "
        + "valid_date, " + "exec_date," + "exec_mode," + "exec_date_s," + "exec_times,"
        + "exec_date_e," + "exec_msg," + "t_acct_cnt," + "t_block_cnt," + "t_blocknot1_cnt,"
        + "t_block_cnt2," + "t_blocknot4_cnt," + "t_block_cnt3" + ", t_blocknot2_cnt ";
    wp.daoTable = "rsk_blockexec";
    wp.whereStr = "where rowid =?";

    setRowid(1, rowid);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料");
      return;
    }
    detlWkdata();
  }

  void detlWkdata() {

    String execMode = wp.colStr("exec_mode");
    wp.colSet("tt_exec_mode", DeCodeRsk.blockExecMode(execMode));

    execMode = wp.colStr("param_type");
    wp.colSet("tt_param_type", DeCodeRsk.blockParamType(execMode));

    String sql1 = " select " + " chin_name " + " from ptr_acct_type " + " where acct_type = ? ";

    sqlSelect(sql1, new Object[] {wp.colStr("acct_type")});

    if (sqlRowNum > 0) {
      wp.colSet("tt_acct_type", sqlStr("chin_name"));
    }

    // --
    int liBlockNot2 = wp.colInt("t_blocknot2_cnt");
    if (liBlockNot2 == 0) {
      wp.colSet("tt_blocknot2", "");
      wp.colSet("t_blocknot2_cnt", "");
    } else {
      wp.colSet("tt_blocknot2", "有其他凍結原因帳戶數(不解凍) T2: ");
    }

  }

}
