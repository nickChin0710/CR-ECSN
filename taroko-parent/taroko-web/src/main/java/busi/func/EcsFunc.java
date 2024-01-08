/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 109-12-24   V1.00.02 Justin        parameterize sql
******************************************************************************/
package busi.func;
/**ecsfunc: 公用程式
 * 2019-0610:  JH    p_seqno >>acno_p_seqno
 * 2018-0831:	JH		get_Curr_rate(xx)
 * 2018-0815:	JH		get_user_deptName()
 * 2017-12xx: get_busi_date
 * 2017-11xx: check_CorpNo
 * 110-01-07  V1.00.06  tanwei        修改意義不明確變量                                                                          *
 * */

public class EcsFunc extends busi.FuncBase {

  public double getCurrRate(String aCurrCode) {
    // -匯率-
    if (empty(aCurrCode))
      return 0;

    strSql = "select exchange_rate from ptr_curr_rate" + " where curr_code =?";
    setString2(1, aCurrCode);
    sqlSelect(strSql);
    if (sqlRowNum <= 0)
      return 0;

    return colNum("exchange_rate");
  }

  public String getUserDeptName(String aUserId) {
    strSql =
        "select A.dept_name" + " from ptr_dept_code A join sec_user B on A.dept_code =B.usr_deptno"
            + " where B.usr_deptno <>''" + " and B.usr_id =?";
    setString2(1, aUserId);
    sqlSelect(strSql);
    if (sqlRowNum > 0)
      return colStr("dept_name");

    return "";
  }

  public String getCurrCodeGl(String currCode) {
    if (empty(currCode))
      currCode = "901";
    strSql = "SELECT curr_code_gl from ptr_currcode" + " where curr_code =?" + commSqlStr.rownum(1);
    setString(1, currCode);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("select ptr_currcode error; kk=%s", currCode);
      return "";
    }
    return colStr("curr_code_gl");
  }

  public boolean checkIdno(String sIdno) {
    // -- Check 身份証字號 Is correct --
    // char01 A~Z, 對應數字 A10,B11,C12,D13,E14,F15,G16,H17,J18,K19,L20
    // M21,N22,P23,Q24,R25,S26,T27,U28,V29,X30,Y31,W32,Z33,I34,O35
    // CHAR02 1,2
    // CHAR03 0,1,2,3
    // CHAR04 ~ 10 IS NUMBER
    // L1(X1X2) D1 D2 D3 D4 D5 D6 D7 D8 D9
    // X1*9+X2*8 *7 *6 *5 *4 *3 *2 *1 *1 *1 SUM / 10 整除
    // ------------------------------------------------------------------------------------

    String lsText = "|ABCDEFGHJKLMNPQRSTUVXYWZIO", c01;
    // , ls_ref[]
    int liPos = 0;
    int[] liChar = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    if (empty(sIdno))
      return false;

    String lsIdno = sIdno.trim();
    if (lsIdno.length() != 10)
      return false;
    c01 = lsIdno.substring(0, 1);
    liPos = lsText.indexOf(c01);
    if (liPos <= 0)
      return false;

    String lsNum = sIdno.substring(1);
    if (this.isNumber(lsNum) == false)
      return false;

    // --blind people ID--
    for (int ii = 1; ii < 10; ii++) {
      liChar[ii + 1] = this.strToInt(lsIdno.substring(ii, ii + 1));
    }
    // --char02
    if (liChar[2] != 1 && liChar[2] != 2)
      return false;

    // --char03
    if (liChar[3] != 0 && liChar[3] != 1 && liChar[3] != 2 && liChar[3] != 3)
      return false;

    // --Set refer number--
    if (eqIgno(c01, "A")) {
      liChar[0] = 1;
      liChar[1] = 0;
    } else if (eqIgno(c01, "B")) {
      liChar[0] = 1;
      liChar[1] = 1;
    } else if (eqIgno(c01, "C")) {
      liChar[0] = 1;
      liChar[1] = 2;
    } else if (eqIgno(c01, "D")) {
      liChar[0] = 1;
      liChar[1] = 3;
    } else if (eqIgno(c01, "E")) {
      liChar[0] = 1;
      liChar[1] = 4;
    } else if (eqIgno(c01, "F")) {
      liChar[0] = 1;
      liChar[1] = 5;
    } else if (eqIgno(c01, "G")) {
      liChar[0] = 1;
      liChar[1] = 6;
    } else if (eqIgno(c01, "H")) {
      liChar[0] = 1;
      liChar[1] = 7;
    } else if (eqIgno(c01, "J")) {
      liChar[0] = 1;
      liChar[1] = 8;
    } else if (eqIgno(c01, "K")) {
      liChar[0] = 1;
      liChar[1] = 9;
    } else if (eqIgno(c01, "L")) {
      liChar[0] = 2;
      liChar[1] = 0;
    } else if (eqIgno(c01, "M")) {
      liChar[0] = 2;
      liChar[1] = 1;
    } else if (eqIgno(c01, "N")) {
      liChar[0] = 2;
      liChar[1] = 2;
    } else if (eqIgno(c01, "P")) {
      liChar[0] = 2;
      liChar[1] = 3;
    } else if (eqIgno(c01, "Q")) {
      liChar[0] = 2;
      liChar[1] = 4;
    } else if (eqIgno(c01, "R")) {
      liChar[0] = 2;
      liChar[1] = 5;
    } else if (eqIgno(c01, "S")) {
      liChar[0] = 2;
      liChar[1] = 6;
    } else if (eqIgno(c01, "T")) {
      liChar[0] = 2;
      liChar[1] = 7;
    } else if (eqIgno(c01, "U")) {
      liChar[0] = 2;
      liChar[1] = 8;
    } else if (eqIgno(c01, "V")) {
      liChar[0] = 2;
      liChar[1] = 9;
    } else if (eqIgno(c01, "X")) {
      liChar[0] = 3;
      liChar[1] = 0;
    } else if (eqIgno(c01, "Y")) {
      liChar[0] = 3;
      liChar[1] = 1;
    } else if (eqIgno(c01, "W")) {
      liChar[0] = 3;
      liChar[1] = 2;
    } else if (eqIgno(c01, "Z")) {
      liChar[0] = 3;
      liChar[1] = 3;
    } else if (eqIgno(c01, "I")) {
      liChar[0] = 3;
      liChar[1] = 4;
    } else if (eqIgno(c01, "O")) {
      liChar[0] = 3;
      liChar[1] = 5;
    }

    // --餘數是否為 0--
    int liMod =
        liChar[0] + liChar[1] * 9 + liChar[2] * 8 + liChar[3] * 7 + liChar[4] * 6 + liChar[5]
            * 5 + liChar[6] * 4 + liChar[7] * 3 + liChar[8] * 2 + liChar[9] + liChar[10];
    if ((liMod % 10) != 0)
      return false;

    return true;
  }

  public boolean xxCheckIdno(String sIdno) {
    String firstChar[] =
        {"A", "B", "C", "D", "E", "F", "G", "R", "S", "T", "U", "V", "X", "Y", "W", "Z", "I", "O"};

    if (empty(sIdno))
      return false;
    if (sIdno.length() != 10)
      return false;

    int inte = -1;
    String colName = String.valueOf(Character.toUpperCase(sIdno.charAt(0)));
    for (int i = 0; i < 18; i++) {
      if (colName.compareTo(firstChar[i]) == 0) {
        inte = i;
      }
    }

    int total = 0;
    int all[] = new int[11];
    String strName = String.valueOf(inte + 10);
    int strNameOne = Integer.parseInt(String.valueOf(strName.charAt(0)));
    int strNameTwo = Integer.parseInt(String.valueOf(strName.charAt(1)));
    all[0] = strNameOne;
    all[1] = strNameTwo;
    for (int j = 2; j <= 10; j++) {
      all[j] = Integer.parseInt(String.valueOf(sIdno.charAt(j - 1)));
    }
    for (int k = 1; k <= 9; k++) {
      total += all[k] * (10 - k);
    }

    total += all[0] + all[10];
    if (total % 10 == 0) {
      return true;
    }
    return false;
  }

  public boolean checkBankNo(String aAcctNo) {
    // --check ICBC 之帳號--
    // -- 0 0 7 0 2 0 1 2 3 4 3
    // -- 4 3 2 8 7 6 5 4 3 2
    // -- 0 0 14 0 14 0 5 8 9 8 MOD(58,11) = 3
    // ====================================================
    String lsAcno = "", lsCode = "";
    int i = 0, liSum = 0, liCk[] = {4, 3, 2, 8, 7, 6, 5, 4, 3, 2};

    lsAcno = commString.right(aAcctNo, 11).trim();
    if (lsAcno.length() < 11)
      return false;
    if (commString.isNumber(lsAcno) == false)
      return false;
    for (int ii = 0; ii < 10; ii++) {
      String colAcno = commString.mid(lsAcno, ii, 1);
      liSum += commString.strToInt(colAcno) * liCk[ii];
    }
    int liMod = liSum % 11;
    lsCode = commString.right("" + liMod, 1);
    if (eqIgno(lsCode, commString.right(lsAcno, 1)))
      return true;
    return false;
  }

  public boolean checkCorpNo(String sCorpNo) {
    /*
     * check 統一編號 ->Rule: X1 + X2 + X3 + X4 + X5 + X6 + X7 + X8 1 2 1 2 1 2 4 1 SUM = (值之數字相加)
     * MOD(SUM,10) = 0 EX: 05100226 --> 1+0+1+4+8+6 = 20 is OK
     */

    String lsCorpno = sCorpNo.trim();
    if (empty(lsCorpno) || lsCorpno.length() != 8) {
      return false;
    }
    if (isNumber(lsCorpno) == false)
      return false;

    double liNum = 0;
    String strNameOne = "", strNameTwo = "";
    int num1 = 0;
    // 1x1
    liNum += strToNum(lsCorpno.substring(0, 1));
    // 2x2
    num1 = (int) this.strToNum(lsCorpno.substring(1, 2)) * 2;
    if (num1 >= 10) {
      liNum += strToNum(("" + num1).substring(0, 1));
      liNum += strToNum(("" + num1).substring(1, 2));
    } else
      liNum += num1;
    // 3x1
    liNum += strToNum(lsCorpno.substring(2, 3));
    // 4x2
    num1 = (int) this.strToNum(lsCorpno.substring(3, 4)) * 2;
    if (num1 >= 10) {
      liNum += strToNum(("" + num1).substring(0, 1));
      liNum += strToNum(("" + num1).substring(1, 2));
    } else
      liNum += num1;
    // 5x1
    liNum += strToNum(lsCorpno.substring(4, 5));
    // 6x2
    num1 = (int) this.strToNum(lsCorpno.substring(5, 6)) * 2;
    if (num1 >= 10) {
      liNum += strToNum(("" + num1).substring(0, 1));
      liNum += strToNum(("" + num1).substring(1, 2));
    } else
      liNum += num1;
    // 7x4
    num1 = (int) this.strToNum(lsCorpno.substring(6, 7)) * 4;
    if (num1 >= 10) {
      liNum += strToNum(("" + num1).substring(0, 1));
      liNum += strToNum(("" + num1).substring(1, 2));
    } else
      liNum += num1;
    // 8x1
    liNum += strToNum(lsCorpno.substring(7, 8));

    return (liNum % 10) == 0;
  }

  public int logOnlineAppr(String pgmId, String fileName, String loginUser, String aprFlag,
      String aprUser) {
    if (empty(pgmId)) {
      errmsg("程式代碼: 不可空白");
    }

    strSql =
        "INSERT INTO log_online_approve (" + " program_id," + " file_name," + " crt_date,"
            + " crt_user," + " apr_flag," + " apr_date," + " apr_user" + " ) VALUES ("
            + " :pgm_id," + " :file_name," + " to_char(sysdate,'yyyymmddhh24miss'),"
            + " :mod_user," + " :apr_flag," + " to_char(sysdate,'yyyymmdd')," + " :apr_user "
            + " )";
    setString("pgm_id", pgmId);
    setString("file_name", fileName);
    setString("mod_user", loginUser);
    setString("apr_flag", aprFlag);
    setString("apr_user", aprUser);
    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert LOG_ONLINE_APPROVE error");
      this.sqlCommit(-1);
      return -1;
    }
    this.sqlCommit(1);

    return 1;
  }

  public int getMcode(String asPSeqno) {
    // double lm_pay_amt=0;
    // int li_mcode =0;
    double lmMcodeAmt = 0;
    int liRCMcode = 0;
    // double lm_min_pay =0;
    busi.DataSet ds = new busi.DataSet();

    // --check acno.payment_rate1 in (0A~0E)--
    String sql1 =
        "select CASE " + " when trim(payment_rate1)='' then 0 "
            + " when UPPER(payment_rate1) = LOWER(payment_rate1) THEN TO_NUMBER(payment_rate1) "
            + " else 0 end as db_mcode " + ", acct_type " + " from act_acno" + " where 1=1 and acno_p_seqno = ? ";
    this.sqlSelect(sql1, new Object[] {asPSeqno});
    if (sqlRowNum == 0) {
      errmsg("查無帳戶資料, acno_p_seqno=" + asPSeqno);
      return -1;
    }
    if (this.colNum("db_mcode") == 0)
      return 0;

    String lsAcctType = colStr("acct_type");

    // -get-minpay-
    sql1 =
        "select mix_mp_balance " + " from ptr_actgeneral_n" + " where 1=1 and acct_type = ? ";
    sqlSelect(sql1, new Object[] {lsAcctType});
    if (sqlRowNum > 0) {
      lmMcodeAmt = colNum("mix_mp_balance");
    }

    sql1 =
        "select A.pay_amt, "
            + " months_between(to_date(B.THIS_ACCT_MONTH,'yyyymm'),to_date(A.acct_month,'yyyymm')) as db_Mcode"
            + " from ptr_workday B, act_acag A" + " where A.stmt_cycle = B.stmt_cycle and A.p_seqno = ? "
            + " order by A.acct_month";

    ds.colList = this.sqlQuery(sql1, new Object[] {asPSeqno});
    for (int ll = 0; ll < sqlRowNum; ll++) {
      ds.listFetch(ll);

      lmMcodeAmt = lmMcodeAmt - ds.colNum("pay_amt");
      if (lmMcodeAmt < 0) {
        liRCMcode = (int) ds.colNum("db_mcode");
        break;
      }
    }
    if (liRCMcode < 0)
      liRCMcode = 0;
    return liRCMcode;
  }

  public String getBusiDate() {
    String sql1 = "select business_date from ptr_businday" + " where 1=1" + commSqlStr.rownum(1);
    sqlSelect(sql1);
    if (sqlRowNum <= 0) {
      return "";
    }
    return this.colStr("business_date");
  }

  public String getPayKey(String aAcctType, String aPSeqno) {
    // -本行銷帳健值-
    String lsPayKey = "";

    String sql1 =
        "select atm_code" + " from ptr_acct_type" + " where 1=1 and acct_type = ? ";
    sqlSelect(sql1, new Object[] {aAcctType});
    if (sqlRowNum <= 0) {
      errmsg("Select ptr_acct_type Error");
      return "";
    }
    String lsAtmCode = colStr("atm_code");

    String lsPSeqno = "8" + aPSeqno.substring(2);
    int[] prefixn = {5, 1, 1, 5, 1, 2, 5, 7, 7, 5, 4, 9, 5};
    int liTmp = 0, liInt = 0, liRem = 0, liInt1 = 0, liInt2 = 0;

    for (int ii = 0; ii < 9; ii++) {
      int colSeqno = this.strToInt(lsPSeqno.substring(ii, ii + 1));
      liTmp = (colSeqno + prefixn[ii]) % 10;
      if (ii == 4) // char-5
        liInt = liInt - liTmp;
      else
        liInt = liInt + liTmp;
      // ddd(""+ii+": tmp="+li_tmp+", int="+li_int);
    }
    // 相乘
    liRem = liInt % 10;
    liInt1 = ((liInt - liRem) / 10) * liRem;
    // 相乘後取餘數
    liRem = liInt1 % 10;
    liInt1 = liRem;

    String lsStr = lsAtmCode + lsPSeqno + liInt1;
    liInt = 0;
    for (int ii = 0; ii < 13; ii++) {
      liTmp = (strToInt(lsStr.substring(ii, ii + 1)) + prefixn[ii]) % 10;
      if (ii == 6) // char-7
        liInt = liInt - liTmp;
      else
        liInt = liInt + liTmp;
    }
    // 相乘
    liRem = liInt % 10;
    liInt2 = ((liInt - liRem) / 10) * liRem;
    // 相乘後取餘數
    liRem = liInt2 % 10;
    liInt2 = liRem;

    lsPayKey = lsStr + liInt2;
    return lsPayKey;
  }

}
