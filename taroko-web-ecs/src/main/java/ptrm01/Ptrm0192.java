/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-03  V1.00.01  Alex       add initButton , add Online Approve        *
* 109-04-20  V1.00.02  Tanwei       updated for project coding standard      *
*                                                                            *
******************************************************************************/
package ptrm01;

import ofcapp.AppMsg;
import ofcapp.BaseEditMulti;
import taroko.com.TarokoCommon;

public class Ptrm0192 extends BaseEditMulti {
 // String kk1 = "";
  Ptrm0192Func func;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
    if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    }

    dddwSelect();
    initButton();

  }


  /*
   * void queryFunc() throws Exception { wp.whereStr =" where 1=1"
   * +sql_col(wp.item_ss("ex_group_code1"),"group_code",">=")
   * +sql_col(wp.item_ss("ex_group_code2"),"group_code","<=")
   * +sql_col(wp.item_ss("ex_billmsg_seq"),"bill_msg_seq",">=") ;
   * wp.whereOrder=" order by group_code ";
   * 
   * wp.queryWhere = wp.whereStr; wp.setQueryMode();
   * 
   * dataRead(); }
   */


  @Override
  public void dataRead() throws Exception {
    wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_group_code1"), "group_code", ">=")
        + sqlCol(wp.itemStr("ex_group_code2"), "group_code", "<=")
        + sqlCol(wp.itemStr("ex_billmsg_seq"), "bill_msg_seq", ">=");
    wp.whereOrder = " order by group_code ";

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    wp.pageControl();
    wp.selectSQL =
        "hex(rowid) as rowid, mod_seqno," + "group_code, " + "group_name, " + "bill_msg_seq";
    wp.daoTable = "ptr_group_code";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY group_code";
    }
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void saveFunc() throws Exception {
    func = new ptrm01.Ptrm0192Func(wp);
    int llOk = 0, llErr = 0;
    // String ls_opt="";

    String[] aaCode = wp.itemBuff("group_code");
    String[] aaMsgseq = wp.itemBuff("bill_msg_seq");
    String[] aaModseq = wp.itemBuff("mod_seqno");
    String[] aaOld = wp.itemBuff("old_data");
    String[] aaDb = wp.itemBuff("db_edit");
    wp.listCount[0] = wp.itemRows("group_code");

    if (this.checkApproveZz() == false)
      return;

    // -insert-
    for (int ll = 0; ll < aaCode.length; ll++) {
      wp.colSet(ll, "ok_flag", "");
      if (empty(aaCode[ll])) {
        continue;
      }

      log("ll=" + ll);
      String lsNew = aaCode[ll] + "," + aaMsgseq[ll];
      // -option-ON-
      if (checkBoxOptOn(ll, aaDb)) {
        if (eqAny(lsNew, aaOld[ll])) {
          continue;
        }


        func.varsSet("group_code", aaCode[ll]);
        func.varsSet("bill_msg_seq", aaMsgseq[ll]);
        func.varsSet("mod_seqno", aaModseq[ll]);
        if (func.dbUpdate() == 1) {
          llOk++;
          wp.colSet(ll, "ok_flag", "V");
        } else {
          llErr++;
          wp.colSet(ll, "ok_flag", "X");
          // err_alert(func.getMsg());
          // break;
        }
      }

    }
    if (llOk > 0) {
      sqlCommit(1);
    }

    alertMsg("資料存檔處理完成; OK=" + llOk + ", ERR=" + llErr);
    dataRead();
  }

  @Override
  public void deleteFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    btnModeAud("xx");

  }

}
