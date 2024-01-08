/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-17  V1.00.00  yash       program initial                            *
* 108-12-02  V1.00.01  Amber	  Update init_button  Authority 			 *
* 109-04-24  V1.00.02  shiyuqi       updated for project coding standard     *   
* 109-07-31  V1.00.03 shiyuqi       修改登入帳號及密碼     *  
* 111-05-24  V1.00.04 JeffKung  Query條件修改為upper(confirm_flag)="Y"                                 *
******************************************************************************/

package bilm01;


import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Bilm0820 extends BaseProc {
  String mExMchtNo = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
      // insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      // updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      // deleteFunc();
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
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 存檔 */
      strAction = "S2";
      dataProcess();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_mcht_no")) == false) {
      wp.whereStr += " and  mcht_no like :mcht_no  ";
      setString("mcht_no", wp.itemStr("ex_mcht_no") + "%");
    }
    if (empty(wp.itemStr("ex_uniform_no")) == false) {
      wp.whereStr += " and  uniform_no like :uniform_no ";
      setString("uniform_no", wp.itemStr("ex_uniform_no") + "%");
    }

    return true;
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
    wp.pageControl();

    wp.selectSQL =
        " mcht_no" + ", data_type" + ", uniform_no" + " , hex(rowid) as rowid, mod_seqno";

    wp.daoTable = "bil_merchant_fd";
    wp.whereOrder = " order by data_type,mcht_no,uniform_no";
    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {


    wp.selectSQL =
        "hex(rowid) as rowid, mod_seqno " + ", mcht_no " + ", data_type" + ", uniform_no";
    wp.daoTable = "bil_merchant_fd";

    getWhereStr();
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }

  @Override
  public void initButton() {
    // if (wp.respHtml.indexOf("_detl") > 0) {
    // this.btnMode_aud();
    // }
    btnUpdateOn(wp.autUpdate());
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      this.dddwList("dddw_mcht_merc",
          "(select mcht_chi_name, mcht_no from bil_merchant  where mcht_status = '1' and upper(confirm_flag) = 'Y' and mcht_type in ('0','1')  union select mcht_chi_name, mcht_no from bil_model_parm )  ",
          "mcht_no", "mcht_chi_name", "where 1=1 order by mcht_no");
      this.dddwList("dddw_mcht_unif",
          "(select mcht_chi_name, uniform_no from bil_merchant  where mcht_status = '1' and upper(confirm_flag) = 'Y' and mcht_type in ('0','1')  union select mcht_chi_name, uniform_no from bil_model_parm )  ",
          "uniform_no", "mcht_chi_name", "where 1=1 order by uniform_no");

    } catch (Exception ex) {
    }
  }

  @Override
  public void dataProcess() throws Exception {
    int llOk = 0, llErr = 0;

    if (strAction.equals("S2")) {

      String[] aaDataType = wp.itemBuff("data_type");
      String[] aaMchtNo = wp.itemBuff("mcht_no");
      String[] aaUniformNo = wp.itemBuff("uniform_no");
      String[] aaRowid = wp.itemBuff("rowid");
      String[] aaModSeqno = wp.itemBuff("mod_seqno");
      String[] aaOpt = wp.itemBuff("opt");

      wp.listCount[0] = aaDataType.length;
      // -check approve-
      if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
        return;
      }


      // -save-
      for (int ll = 0; ll < aaDataType.length; ll++) {

        if (checkBoxOptOn(ll, aaOpt)) {
          if (empty(aaRowid[ll])) {
            wp.colSet(ll, "ok_flag", "V");
            llOk++;
            continue;
          } else {

            String delSql =
                "delete bil_merchant_fd where hex(rowid) = :rowid  and mod_seqno = :mod_seqno  ";
            setString("rowid", aaRowid[ll]);
            setString("mod_seqno", aaModSeqno[ll]);
            sqlExec(delSql);
            if (sqlRowNum <= 0) {
              llErr++;
              wp.colSet(ll, "ok_flag", "!");
              continue;
            } else {
              llOk++;
              wp.colSet(ll, "ok_flag", "V");
              continue;
            }
          }
        }

        if (empty(aaRowid[ll])) {
          // insert
          String lsIns = "insert into bil_merchant_fd ( " + " data_type,  " + "mcht_no, "
              + "uniform_no," + "mod_time," + "mod_user," + "mod_pgm," + "mod_seqno" + ")values(  "
              + " :data_type," + " :mcht_no," + " :uniform_no," + " sysdate," + " :mod_user,"
              + " :mod_pgm," + " 1 ) ";
          setString("data_type", aaDataType[ll]);
          setString("mcht_no", aaMchtNo[ll]);
          setString("uniform_no", aaUniformNo[ll]);
          setString("mod_user", wp.loginUser);
          setString("mod_pgm", wp.itemStr("MOD_PGM"));
          sqlExec(lsIns);
          if (sqlRowNum <= 0) {
            llErr++;
            wp.colSet(ll, "ok_flag", "!");
          } else {
            llOk++;
            wp.colSet(ll, "ok_flag", "V");
          }

        }

      }


    }
    sqlCommit(llOk > 0 ? 1 : 0);
    alertMsg("資料存檔處理完成; OK = " + llOk + ", ERR = " + llErr);

  }



}
