/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE             Version    AUTHOR      DESCRIPTION                               *
* ---------        --------     ----------     ------------------------------------------ *
* 110/12/28    V1.00.00   Machao     全體員工年招攬卡年度總目標參數檔維護 
* 111/03/22    V1.00.01   machao     页面bug调整                                 *    
* 112/03/03    V1.00.02   Zuwei Si   覆核=N時可修改，新增失敗，新增修改時，如覆核主管密碼有輸入，check各項明細是否有輸入                                 *    
******************************************************************************/
package mktm02;

import java.util.Arrays;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Mktm3020 extends BaseEdit {
String rowid;
int qFrom = 0;
 String acctYear;
 String aprFlag;
@Override
public void actionFunction(TarokoCommon wr) throws Exception {
	super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    switch (wp.buttonCode) {
        case "X":
            /* 轉換顯示畫面 */
            strAction = "new";
            clearFunc();
            break;
        case "Q":
            /* 查詢功能 */
            strAction = "Q";
            queryFunc();
            break;
        case "R":
            // -資料讀取-
            strAction = "R";
            dataRead();
            break;
        case "A":
            /* 新增功能 */
            strAction = "A";
            saveFunc();
            break;
        case "U":
            /* 更新功能 */
            strAction = "U";
            saveFunc();
            break;
        case "D":
            /* 刪除功能 */
            strAction = "D";
            saveFunc();
            break;
        case "R2":
            /* 參數頁面 資料讀取 */
            strAction = "R2";
            dataRead2();
            break;
        case "U2":
            /* 參數頁面 資料讀取 */
            strAction = "U2";
            dataSave2();
            break;
        case "M":
            /* 瀏覽功能 :skip-page */
            queryRead();
            break;
        case "S":
            /* 動態查詢 */
            querySelect();
            break;
        case "L":
            /* 清畫面 */
            strAction = "";
            clearFunc();
            break;
        default:
            break;
    }

    dddwSelect();
    initButton();
	
}

@Override
public void queryFunc() throws Exception {
	 wp.whereStr = " where 1=1 "
             + sqlCol(wp.itemStr("ex_acct_year"), "acct_year");
	 if (!wp.itemEq("ex_apr_flag", "0")) {
		 wp.whereStr += sqlCol(wp.itemStr("ex_apr_flag"), "apr_flag");
	   }
     wp.queryWhere = wp.whereStr;
     wp.setQueryMode();

     queryRead();
	
}
@Override
public void queryRead() throws Exception {
	wp.pageControl();
    wp.selectSQL =
        "acct_year, " + "target_card_cnt, " + "branch_cnt, " + "employee_cnt, " + "apr_flag";
    wp.daoTable = "mkt_year_target";
    wp.whereOrder = " order by acct_year ";
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr("此條件查無資料");
      return;
    }
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
}
@Override
public void querySelect() throws Exception {
//	rowid = itemKk("data_k2");
    qFrom = 1;
	if (wp.colEq("data_k3", "N")) {
	  wp.alertMesg("資料待覆核");
	  }
    
    dataRead();
	
}


//详情页面查询
@Override
public void dataRead() throws Exception {
	if (qFrom == 0) {
        if (wp.itemStr("ex_acct_year").length() == 0) {
            alertErr("年度必須輸入");
            return;
        }
    }

	wp.selectSQL ="hex(rowid) as rowid,acct_year, target_card_cnt, branch_cnt, employee_cnt, acct_type_flag, group_code_flag,"
			+ "card_type_flag, branch_flag, crt_user, crt_date,  mod_user, TO_CHAR(mod_time,'YYYY/MM/DD') as mod_t, apr_flag, apr_date";
	
    wp.daoTable = " mkt_year_target";
    wp.whereStr = " where 1=1 ";
    if (qFrom == 0) {
        wp.whereStr = wp.whereStr + sqlCol(wp.itemStr("ex_acct_year"), "acct_year");
    } else if (qFrom == 1) {
    	wp.whereStr = wp.whereStr + sqlCol(wp.itemStr("data_k2"), "acct_year");
    }
//    String mod = wp.getValue("mod_t");
    
    pageSelect();
    if (sqlNotFind()) {
        alertErr2("此條件查無資料");
    }

	
}
@Override
public void saveFunc() throws Exception {
	 if (isAdd()) {
         if (wp.itemEmpty("approval_user") && wp.itemEmpty("approval_passwd")) {
             wp.itemSet("apr_flag", "N");
         } else {
             if (checkApproveZz()) {
                 // 如果 ACCT_TYPE_FLAG=1 or 2 顥示” 帳戶類別,至少要輸入1筆”
                 String acctYear = wp.itemStr("acct_year");
                 String acctTypeFlag = wp.itemStr("acct_type_flag");
                 String groupCodeFlag = wp.itemStr("group_code_flag");
                 String cardTypeFlag = wp.itemStr("card_type_flag");
                 String branchFlag = wp.itemStr("branch_flag");
                 boolean ok = false;
                 ok = checkDetl(acctTypeFlag, "帳戶類別", acctYear, "01");
                 if (!ok) {
                     return;
                 }
                 ok = checkDetl(groupCodeFlag, "團體代碼", acctYear, "02");
                 if (!ok) {
                     return;
                 }
                 ok = checkDetl(cardTypeFlag, "卡片種類", acctYear, "03");
                 if (!ok) {
                     return;
                 }
                 ok = checkDetl(branchFlag, "分行", acctYear, "04");
                 if (!ok) {
                     return;
                 }
                 wp.itemSet("apr_flag", "Y");
             } else {
                 return;
             }
         }
     } else if (isUpdate()) {
         if (wp.itemEmpty("approval_user") && wp.itemEmpty("approval_passwd")) {
             wp.itemSet("apr_flag", "N");
         } else {
             if (checkApproveZz()) {
                 // 如果 ACCT_TYPE_FLAG=1 or 2 顥示” 帳戶類別,至少要輸入1筆”
                 String acctYear = wp.itemStr("acct_year");
                 String acctTypeFlag = wp.itemStr("acct_type_flag");
                 String groupCodeFlag = wp.itemStr("group_code_flag");
                 String cardTypeFlag = wp.itemStr("card_type_flag");
                 String branchFlag = wp.itemStr("branch_flag");
                 boolean ok = false;
                 ok = checkDetl(acctTypeFlag, "帳戶類別", acctYear, "01");
                 if (!ok) {
                     return;
                 }
                 ok = checkDetl(groupCodeFlag, "團體代碼", acctYear, "02");
                 if (!ok) {
                     return;
                 }
                 ok = checkDetl(cardTypeFlag, "卡片種類", acctYear, "03");
                 if (!ok) {
                     return;
                 }
                 ok = checkDetl(branchFlag, "分行", acctYear, "04");
                 if (!ok) {
                     return;
                 }
                 wp.itemSet("apr_flag", "Y");
             } else {
                 return;
             }
         }
     } else if (isDelete() && wp.itemEq("apr_flag", "Y")) {
         if (!checkApproveZz()) {
             return;
         }
     }

     mktm02.Mktm3020Func func = new mktm02.Mktm3020Func(wp);
     rc = func.dbSave(strAction);
     sqlCommit(rc);

     if (rc != 1) {
         alertErr(func.getMsg());
     } else {
         if (isUpdate()) {
             qFrom = 1;
             rowid = wp.itemStr("rowid");
             saveAfter(true);
         } else {
             saveAfter(false);
         }
     }
	
}


// 3. 新增或修改(覆核=N)後, 按下存檔, 如果有輸入覆核主管、帳密,存檔前,要檢核以下4項,任一項檢核不符, 不可異動(覆核=Y)
private boolean checkDetl(String flag, String cname, String acctYear, String dataType) {
    if (!"1".equals(flag) && !"2".equals(flag)) {
        return true; 
    }
    String strSql = "select acct_year, data_type "
            + "from mkt_year_dtl "
            + "where acct_year = ? "
            + "and data_type = ? ";

    Object[] params = new Object[] {
            acctYear, dataType
    };
    sqlSelect(strSql, params);
    if(sqlRowNum <= 0) {
        errmsg(cname + " 至少要輸入1筆");
        return false;
    }
    return true;
}
public void saveAfter(boolean bRetrieve) throws Exception {
    if (rc != 1) {
        return;
    }
    if (isAdd()) {
        if (bRetrieve) {
            dataRead3();
        } else {
            clearFunc();
        }
    } else if (isUpdate()) {
        if (bRetrieve) {
            dataRead3();
        } else {
            modSeqnoAdd();
        }
    } else {
        clearFunc();
    }
}

public void dataSave2() throws Exception {
    int llOk = 0;
    int llErr = 0;

    String lsType = wp.itemStr("data_type");
    if (empty(lsType)) {
        errmsg("無法取得資料類別[data_type]");
        return;
    }

    mktm02.Mktm3020Func func = new mktm02.Mktm3020Func(wp);

    String[] aaCode = wp.itemBuff("ex_data_code1");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = aaCode.length;
    wp.colSet("row_num", "" + aaCode.length);

    // -check duplication-
    int ii = -1;
    for (String tmpStr : aaCode) {
        ii++;
        wp.colSet(ii, "ok_flag", "");
        // -option-ON-
        if (checkBoxOptOn(ii, aaOpt)) {
            aaCode[ii] = "";
            continue;
        }

        if (ii != Arrays.asList(aaCode).indexOf(tmpStr)) {
            wp.colSet(ii, "ok_flag", "!");
            llErr++;
        }
    }

    if (llErr > 0) {
        alertErr("數據重複: " + llErr);
        return;
    }

    // -delete no-approve-
    if (func.deleteAllDetl(lsType) < 0) {
        this.dbRollback();
        alertErr(func.getMsg());
        return;
    }

    for (int ll = 0; ll < aaCode.length; ll++) {
        wp.colSet(ll, "ok_flag", "");

        // -option-ON-
        if (checkBoxOptOn(ll, aaOpt)) {
            llOk++;
            continue;
        }

        if (empty(aaCode[ll])) {
            llOk++;
            continue;
        }

        func.varsSet("ex_data_code1", aaCode[ll]);
        if (func.insertDetl(lsType) == 1) {
            llOk++;
        } else {
            llErr++;
        }
    }

    if (llOk > 0) {
        sqlCommit(1);
    }

    alertMsg("資料存檔處理完成; OK=" + llOk + ", ERR=" + llErr);
    dataRead2();
}
public void dataRead2() throws Exception {
    String acctyear = wp.itemStr("acct_year");
    if (empty(acctyear)) {
    	acctyear = wp.itemStr("acct_year");
    }

    String rowId2 = wp.itemStr("data_k2");
    if (empty(rowId2)) {
        rowId2 = wp.itemStr("data_k2");
    }

    switch (wp.respHtml) {
        case "mktm3020_detl_01":
            wp.sqlCmd = "select a.acct_year, a.data_type, a.data_code1||'_'||c.chin_name as data_code1, " +
                    "a.data_code1 as ex_data_code1, b.apr_flag, hex(b.rowid) as rowid " +
                    "from mkt_year_dtl a " +
                    "inner join mkt_year_target b on a.acct_year = b.acct_year " +
                    "inner join ptr_acct_type c on a.data_code1 = c.acct_type " +
                    "where 1 = 1 and data_type = '01' " +
                    sqlCol(acctyear, "a.acct_year") +
                    sqlCol(rowId2, "hex(b.rowid)") +
                    "union " +
                    "select a.acct_year, a.data_type, a.data_code1||'_'||c.chin_name as data_code1, " +
                    "a.data_code1 as ex_data_code1, b.apr_flag, hex(b.rowid) as rowid " +
                    "from mkt_year_dtl a " +
                    "inner join mkt_year_target b on a.acct_year = b.acct_year " +
                    "inner join dbp_acct_type c on a.data_code1 = c.acct_type " +
                    "where 1 = 1 and data_type = '01' " +
                    sqlCol(acctyear, "a.acct_year") +
                    sqlCol(rowId2, "hex(b.rowid)");
            break;
        case "mktm3020_detl_02":
            wp.sqlCmd = "select a.acct_year, a.data_type, a.data_code1||'_'||c.group_name as data_code1, " +
                    "a.data_code1 as ex_data_code1, b.apr_flag, hex(b.rowid) as rowid " +
                    "from mkt_year_dtl a " +
                    "inner join mkt_year_target b on a.acct_year = b.acct_year " +
                    "inner join ptr_group_code c on a.data_code1 = c.group_code " +
                    "where 1 = 1 and data_type = '02' " +
                    sqlCol(acctyear, "a.acct_year") +
                    sqlCol(rowId2, "hex(b.rowid)");
            break;
        case "mktm3020_detl_03":
            wp.sqlCmd = "select a.acct_year, a.data_type, a.data_code1||'_'||c.name as data_code1, " +
                    "a.data_code1 as ex_data_code1, b.apr_flag, hex(b.rowid) as rowid " +
                    "from mkt_year_dtl a " +
                    "inner join mkt_year_target b on a.acct_year = b.acct_year " +
                    "inner join ptr_card_type c on a.data_code1 = c.card_type " +
                    "where 1 = 1 and data_type = '03' " +
                    sqlCol(acctyear, "a.acct_year") +
                    sqlCol(rowId2, "hex(b.rowid)");
            break;
        case "mktm3020_detl_04":
            wp.sqlCmd = "select a.acct_year, a.data_type, a.data_code1||'_'||c.brief_chi_name as data_code1, " +
                    "a.data_code1 as ex_data_code1, b.apr_flag, hex(b.rowid) as rowid " +
                    "from mkt_year_dtl a " +
                    "inner join mkt_year_target b on a.acct_year = b.acct_year " +
                    "inner join gen_brn c on a.data_code1 = c.branch " +
                    "where 1 = 1 and data_type = '04' " +
                    sqlCol(acctyear, "a.acct_year") +
                    sqlCol(rowId2, "hex(b.rowid)");
            break;
        default:
            break;
    }

    this.selectNoLimit();
    pageQuery();
    if (sqlRowNum == 0) {
        this.selectOK();
    }

    wp.setListCount(1);
    wp.colSet("row_num", "" + wp.selectCnt);
}
public void dataRead3() {
	String acct = wp.itemStr("ex_acct_year");
	wp.selectSQL ="hex(rowid) as rowid,acct_year, target_card_cnt, branch_cnt, employee_cnt, acct_type_flag, group_code_flag,"
			+ "card_type_flag, branch_flag, crt_user, crt_date,  mod_user, TO_CHAR(mod_time,'YYYY/MM/DD') as mod_t, apr_flag, apr_date";
	
    wp.daoTable = " mkt_year_target";
    wp.whereStr = " where 1=1 ";
    wp.whereStr = wp.whereStr + sqlCol(wp.itemStr("ex_acct_year"), "acct_year");
    pageSelect();
    if (sqlNotFind()) {
        alertErr2("此條件查無資料");
    }
}

@Override
public void initButton() {
	if (wp.respHtml.indexOf("_detl") > 0) {
        this.btnModeAud();
        if (wp.colEq("apr_flag", "Y")) {
            buttonOff("btnUpdate_disable");
            buttonOff("btnDelete_disable");
        } 
        if (wp.colEq("apr_flag", "N") && !wp.colEmpty("rowid")) {
            btnUpdateOn(true);
        }
    }

    if (empty(wp.colStr("rowid"))) {
        buttonOff("btnparm_disable");
    }

    if (wp.respHtml.indexOf("_detl_0") > 0) {
        if (wp.colEq("apr_flag", "Y")) {
            buttonOff("btnUpdate_disable");
            buttonOff("newDetail_disable");
        }
    }

    int rr;
    rr = wp.listCount[0];
    wp.colSet(0, "IND_NUM", "" + rr);
	
}
public void dddwSelect() {
    try {
        switch (wp.respHtml) {
            case "mktm3020_detl_01":
                wp.initOption = "";
                wp.optionKey = "";
                dddwList("dddw_ptr_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1 = 1");
                wp.initOption = "";
                wp.optionKey = "";
                dddwList("dddw_dbp_acct_type", "dbp_acct_type", "acct_type", "chin_name", "where 1 = 1");
                break;
            case "mktm3020_detl_02":
                wp.initOption = "";
                wp.optionKey = "";
                dddwList("dddw_ptr_group_code", "ptr_group_code", "group_code", "group_name", "where 1 = 1");
                break;
            case "mktm3020_detl_03":
                wp.initOption = "";
                wp.optionKey = "";
                dddwList("dddw_ptr_card_type", "ptr_card_type", "card_type", "name", "where 1 = 1");
                break;
            case "mktm3020_detl_04":
                wp.initOption = "";
                wp.optionKey = "";
                dddwList("dddw_gen_brn", "gen_brn", "branch", "brief_chi_name", "where 1 = 1");
                break;
            default:
                break;
        }
    } catch (Exception exception) {
        System.out.println("error [Mktm3020] : " + exception.getMessage());
    }
}
}
