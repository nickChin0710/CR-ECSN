package secm01;
/**
 * 2019-1204   JH    authority
 * 109-04-20  shiyuqi       updated for project coding standard
 * 109-07-17  JustinWu   modify errMsg
 * *  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
 * */
import java.util.Arrays;

import ofcapp.BaseAction;

public class Secm2020 extends BaseAction {
  int llOk = 0, llErr = 0;
  int ii = 0;

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
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_group_id"), "group_id", "like%")
        + sqlCol(wp.itemStr2("ex_group_name"), "group_name", "like%");


    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " group_id ," + " group_name ," + " mod_seqno ,"
    // + " log_mark ,"
        + " hex(rowid) as rowid , " + " group_id||group_name as old_data ";
    wp.daoTable = "sec_workgroup";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by group_id ";
    pageQuery();
    wp.colSet("IND_NUM", "" + wp.selectCnt);
    wp.setListCount(1);

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.listCount[1] = wp.dataCnt;
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
    Secm2020Func func = new Secm2020Func();
    func.setConn(wp);
    String[] opt = wp.itemBuff("opt");
    String[] lsGroupId = wp.itemBuff("group_id");
    // String[] aa_log_mark =new String[ls_group_id.length];
    String[] lsGroupName = wp.itemBuff("group_name");
    String[] lsOldData = wp.itemBuff("old_data");
    String[] lsRowid = wp.itemBuff("rowid");
    wp.listCount[0] = lsGroupId.length;
    // for(int aa=0;aa<ls_group_id.length;aa++){
    // wp.col_set(aa,"log_mark", wp.item_ss("log_mark-"+aa));
    // }
    func.varsSet("approveUser",wp.itemStr("approval_user"));
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }
    ii = -1;
    for (String tmpStr : lsGroupId) {
      ii++;
      wp.colSet(ii, "ok_flag", "");
      // -option-ON-
      if (checkBoxOptOn(ii, opt)) {
        continue;
      }

      if (ii != Arrays.asList(lsGroupId).indexOf(tmpStr)) {
        wp.colSet(ii, "ok_flag", "!");
        llErr++;
      }

    }
    if (llErr > 0) {
      alertErr("資料值重複: " + llErr);
      return;
    }

    for (int rr = 0; rr < lsGroupId.length; rr++) {
      // String ls_log_mark=wp.item_nvl("log_mark-"+rr,"N");
      // wp.col_set(rr, "log_mark",ls_log_mark);
      // func.vars_set("log_mark", ls_log_mark);
      func.varsSet("group_id", lsGroupId[rr]);
      func.varsSet("group_name", lsGroupName[rr]);
      if (checkBoxOptOn(rr, opt)) {
        func.varsSet("rowid", lsRowid[rr]);
        
        if (func.dbDeleteDetl() == 1) {
          dbCommit();
          llOk++;
        } else {
          dbRollback();
          llErr++;
        }
      } else if (!eqIgno(lsOldData[rr], lsGroupId[rr] + lsGroupName[rr]) && !empty(lsOldData[rr])) {
        func.varsSet("rowid", lsRowid[rr]);
        if (func.dbUpdateDetl() == 1) {
          log("A:" + func.dbUpdateDetl());
          llOk++;
        } else {
          log("rowid:" + lsRowid[rr]);
          log("A:" + func.dbUpdateDetl());
          llErr++;
        }
      } else if (eqIgno(lsOldData[rr], lsGroupId[rr] + lsGroupName[rr])) {
        continue;
      } else if (empty(lsOldData[rr])) {
        if (func.dbInsertDetl() == 1) {
          llOk++;
        } else {
          llErr++;
        }
      }
    }
    
    
    if (llOk > 0) {
      sqlCommit(1);
    }
    queryFunc();
    wp.respMesg = "資料存檔處理完成; OK=" + llOk + ", ERR=" + llErr;

  }

  @Override
  public void initButton() {
    btnUpdateOn(wp.autUpdate());
  }

  @Override
  public void initPage() {
    wp.colSet("IND_NUM", "" + 0);

  }

}
