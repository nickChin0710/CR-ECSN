package cmsm01;
/** 歸戶餘額查詢
 * V.2018-0809
* 109-04-27  shiyuqi       updated for project coding standard     *  
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名
* 110-01-05  V1.00.02  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *                                                                                       * 
 * */

import java.io.IOException;
import busi.FuncAction;

public class Cmsq0010Func extends FuncAction {
  taroko.base.CommDate commDate = new taroko.base.CommDate();

  int liCnt = 0, ilRow = 0;
  boolean ibFirst = true;
  // --放入資料
  String isApcMastId = "", isApcAcctId = "", isApcErrcode = "";
  String isApcProcDesc = "", apcRtnMastid = "";
  int isPoscnt = 0, isApcRtncnt = 0;

  private String isApcRtnsubid = "";
  private String isApcSubidName = "";

  @Override
  public void dataCheck() {
    // TODO Auto-generated method stub

  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  // ---------------------------------------------------------------------------
  public int readData(String aIdno) {
    boolean lbFirst = true;
    int liCnt = 0;
    if (empty(aIdno)) {
      errmsg("歸戶ID: 不可空白");
      return rc;
    }

    String lsMasterId = aIdno; // wp.item_ss("ex_mast_id");

    String lsSubId = "";
    String lsCnt = "0001";
    String lsCtlData = "00000";
    String lsAcctId = wp.itemNvl("ex_acct_id", lsMasterId);

    ecsfunc.EcsCallbatch ooAppc = new ecsfunc.EcsCallbatch(wp);

    liCnt = 0;
    while (liCnt < 99) {
      liCnt++;
      if (eqIgno(lsCnt, "9999"))
        break;

      String lsParm = "NB00PH11       ";
      lsParm += commString.mid(lsMasterId, 0, 10);
      lsParm += "0000 ";
      lsParm += commString.lpad(lsSubId, 10, " ");
      lsParm += " ";
      lsParm += lsCnt;
      lsParm += " ";
      lsParm += commString.lpad(lsCtlData, 5, "0");
      lsParm += "  ";
      lsParm += commString.lpad(lsAcctId, 10, " ");

      if (lbFirst) {
        isApcMastId = lsMasterId;
        isPoscnt = 1;
        isApcAcctId = lsAcctId;
      }

      // --call SOCKET--
      // int li_recv=1000; //PB:844
      String lsRcvdata = "";
      try {
        lsRcvdata = ooAppc.commAppc(lsParm, 0);
        if (lsRcvdata.length() == 0 && lbFirst) {
          isApcProcDesc = commString.left(lsRcvdata, 255);
          insertCmsAppcAcct();
          errmsg("資訊處未回應");
          break;
        }
      } catch (IOException ex) {
        errmsg("call APPC fail");
        break;
      }

      String param = "";
      String[] aaData = new String[] {lsRcvdata, ""};
      commString.token2(aaData, 7);
      String lsErrcode = commString.token2(aaData, 4).trim(); // -errcode-
      param = commString.token2(aaData, 10).trim(); // -Master-ID-
      if (lbFirst)
        apcRtnMastid = param;
      lsSubId = commString.token2(aaData, 10).trim(); // -SUB-ID-
      param = commString.token2(aaData, 76).trim();
      if (lbFirst)
        isApcSubidName = param;
      lsCnt = commString.token2(aaData, 4).trim(); // CNT

      // addLog(ls_rcvdata);
      if (lbFirst) {
        isApcErrcode = lsErrcode;
        isApcRtnsubid = lsSubId;
        isApcRtncnt = commString.strToInt(lsCnt);
        insertCmsAppcAcct();
      }

      if (!empty(lsErrcode) && !eqIgno(lsErrcode, "E000")) {
        errmsg("歸戶餘額查詢失敗 : " + lsErrcode);
        break;
      }
      // --set DW-data----------
      wfSetData(lsRcvdata);

      lbFirst = false;
    }

    return rc;
  }

  // ---------------------------------------------------------------------------
  // void addLog(String a_rcvdata) {
  // String ss[] = new String[2];
  // ss[0] = a_rcvdata;
  // ss = commString.token(ss, 7);
  // ls_temp = ss[1];
  // ss = commString.token(ss, 4);
  // ls_errcode = ss[1];
  // ss = commString.token(ss, 10);
  // if (lb_first)
  // ls_apc_rtnmasterid = ss[1];
  // ss = commString.token(ss, 10);
  // is_sub_id = ss[1];
  // ss = commString.token(ss, 76);
  // wp.col_set("db_cname", ss[1].trim());
  // if (lb_first)
  // ls_apc_subid_name = ss[1].trim();
  // ss = commString.token(ss, 4);
  // is_cnt = ss[1];
  // }

  // ---------------------------------------------------------------------------
  void wfSetData(String aRcvdata) {
    log(aRcvdata);

    String lsDetail = "", isCtlData = "", lsCurrGroup = "", lsCtGroup = "", lsOtherInfo = "";
    String[] tt = new String[2];
    tt[0] = aRcvdata;
    String[] aaDetail = new String[2];
    String[] aaData = new String[2];
    String[] aaCurr = new String[2];
    String[] aaGroup = new String[2];
    String[] aaOther = new String[2];

    aaDetail[0] = commString.mid2(aRcvdata, 0, 320); // commString.token(tt, 320);
    isCtlData = commString.mid2(aRcvdata, 320, 5);
    aaCurr[0] = commString.mid2(aRcvdata, 325, 16);
    aaGroup[0] = commString.mid2(aRcvdata, 341, 96);
    aaOther[0] = commString.mid2(aRcvdata, 437);
    int llRow = ilRow;
    for (int ii = 0; ii < 8; ii++) {
      if (empty(lsDetail))
        break;
      // -detail-
      String lsData = commString.token2(aaDetail, 40);
      String lsData1 = commString.mid2(lsData, 0, 11);
      if (empty(lsData1))
        continue;

      int ll = llRow + ii;
      wp.colSet(ii, "ex_acct", lsData1);
      wp.colSet(ii, "ex_bal_sign", commString.mid2(lsData, 11, 1));
      lsData1 = commString.mid2(lsData, 12, 14);
      wp.colSet(ii, "ex_bal", commString.strToNum(lsData1) / 100);
      lsData1 = commString.mid2(lsData, 16, 14);
      wp.colSet(ii, "ex_ckbk", commString.strToNum(lsData1) / 100);
      // -CTL_DATA-
      wp.colSet(ii, "ex_ctldata", isCtlData.trim());
      // --幣別--
      lsData1 = commString.token2(aaCurr, 2);
      wp.colSet(ii, "ex_curr", lsData1.trim());
      // --CT_GROUP--
      lsData = commString.token2(aaGroup, 12);
      lsData1 = commString.mid2(lsData, 0, 6);
      if (this.strToInt(lsData1) >= 990601) {
        wp.colSet(ii, "ex_duedate", commDate.twToAdDate(lsData1));
      } else
        wp.colSet(ii, "ex_duedate", commDate.twToAdDate("1" + lsData1));
      lsData1 = commString.mid2(lsData, 6);
      wp.colSet(ii, "ex_rate", commString.strToNum(lsData1) / 10000);
      // --OTHER_INFO--
      lsData = commString.token2(aaOther, 37);
      lsData1 = commString.mid2(lsData, 0, 12);
      wp.colSet(ii, "ex_stoppay", commString.strToNum(lsData1) / 100);
      lsData1 = commString.mid2(lsData, 12, 1);
      wp.colSet(ii, "ex_micrflag", lsData1.trim());
      lsData1 = commString.mid2(lsData, 13, 12);
      wp.colSet(ii, "ex_micramt", commString.strToNum(lsData1) / 100);
      lsData1 = commString.mid2(lsData, 25, 12);
      wp.colSet(ii, "ex_micramt", commString.strToNum(lsData1) / 100);
      ilRow++;
    }
  }

  // ---------------------------------------------------------------------------
  void insertCmsAppcAcct() {
    sql2Insert("cms_appc_acct");
    addsqlParm("mod_date", commSqlStr.sqlDateTime);
    addsqlParm(",?", ", mod_user", modUser);
    addsqlParm(",?", ", mod_pgm", modPgm);
    addsqlParm(",?", ", apc_type", "NB00");
    addsqlParm(",?", ", apc_code", "PH11");
    addsqlParm(",?", ", apc_masterid", isApcMastId);
    addsqlParm(",?", ", apc_poscnt", 1);
    addsqlParm(",?", ", apc_ctldata", "00000");
    addsqlParm(",?", ", apc_acctid", isApcAcctId);
    addsqlParm(",?", ", apc_errcode", isApcErrcode);
    addsqlParm(",?", ", apc_rtnmasterid", apcRtnMastid);
    addsqlParm(",?", ", apc_rtnsubid", isApcRtnsubid);
    addsqlParm(",?", ", apc_subid_name", isApcSubidName);
    addsqlParm(",?", ", apc_rtncnt", isApcRtncnt);
    addsqlParm(",?", ", apc_procdesc", isApcProcDesc);

    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum <= 0) {
      wp.log("insert cms_appc_acct error");
    }

  }

}
