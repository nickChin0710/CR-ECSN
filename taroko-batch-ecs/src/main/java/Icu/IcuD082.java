/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR               DESCRIPTION                    *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  111/08/01  V1.00.00    Yang Bo            program initial                  *
 *  111/08/22  V1.00.01    Yang Bo            modify logic about verify code   *
 *  111/08/22  V1.00.02    Yang Bo            fix compiler virtual code bug    *
 ******************************************************************************/
package Icu;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;

public class IcuD082 extends AccessDAO {
    private final String progname = "補銷帳編號及虛擬帳號處理程式  111/08/22  V1.00.02";
    private final String prgmId = "IcuD082";
    CommCrdRoutine comcr = null;
    CommCrd comc = new CommCrd();
    String tempUser = "";
    int totalCnt = 0;
    int chgDataNum = 0;
    int noChgDataNum = 0;

    // ACT_ACNO表數據字段
    private String actAcnoPSeqno = "";
    private String actIdPSeqno = "";
    private String actCardIndicator = "";
    private String actAcnoFlag = "";
    private String actPaymentNo = ""; // 銷帳編號
    private String actPaymentNoII = ""; // 虛擬帳號

    // CRD_IDNO表數據字段
    private String crdIdNo; // 身份證號碼

    // CRD_CARD表數據字段
    private String crdCardNo; // 卡號


    public static void main(String[] args) throws Exception {
        IcuD082 proc = new IcuD082();
        int retCode = proc.mainProcess(args);
        System.exit(retCode);
    }

    public int mainProcess(String[] args) throws Exception {
        String callBatchSeqno = "";

        try {
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            tempUser = comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, callBatchSeqno);

            selectAccountBasicData();

            showLogMessage("I", "", String.format("處理日期: %s, 檔案資料更新筆數: %d筆", sysDate, chgDataNum));
            showLogMessage("I", "", String.format("總處理筆數: %d筆, 未更新筆數: %d筆", totalCnt, noChgDataNum));
            showLogMessage("I", "", "執行結束");
            comcr.hCallErrorDesc = "程式執行結束";
            comcr.callbatchEnd();
            return 0;
        } catch (Exception e) {
            expMethod = "mainProcess";
            expHandle(e);
            return exceptExit;
        } finally {
            finalProcess();
        }
    }


    // 讀取帳戶基本資料主檔
    private void selectAccountBasicData() throws Exception {
        // 讀取銷帳編號及虛擬帳號皆為空白的資料
        sqlCmd = " SELECT ACNO_P_SEQNO, " +
                "        ID_P_SEQNO, " +
                "        CARD_INDICATOR, " +
                "        ACNO_FLAG " +
                " FROM ACT_ACNO" +
                " WHERE ACNO_FLAG IN ('1','3')" +
                "   AND PAYMENT_NO = '' " +
                "   AND PAYMENT_NO_II = '' " +
                "   AND ID_P_SEQNO != '' " +
                "   AND ACNO_P_SEQNO != '' ";

        this.openCursor();
        while (fetchTable()) {
            actAcnoPSeqno = getValue("ACNO_P_SEQNO");
            actIdPSeqno = getValue("ID_P_SEQNO");
            actCardIndicator = getValue("CARD_INDICATOR");
            actAcnoFlag = getValue("ACNO_FLAG");

            updateData();
            totalCnt++;
        }
        this.closeCursor();
    }

    private void updateData() throws Exception {
        crdIdNo = "";
        crdCardNo = "";
        actPaymentNo = "";
        actPaymentNoII = "";

        if ("1".equals(actAcnoFlag)) {
            selectIdNo();
        }

        if ("3".equals(actAcnoFlag)) {
            selectCardNo();
        }

        // 編列銷帳編號
        compileChargeOffNum();
        // 編列虛擬編號
        compileVirtualSerialNum();

        updateActAcno();
    }

    /*  讀取一般戶自然人ID  */
    private void selectIdNo() throws Exception {
        sqlCmd = " SELECT ID_NO " +
                " FROM CRD_IDNO " +
                " WHERE ID_P_SEQNO = ?";
        setString(1, actIdPSeqno);

        selectTable();
        if ("Y".equals(notFound)) {
            showLogMessage("I", "", "ERROR : 讀取CRD_IDNO[ID_P_SEQNO=" + actIdPSeqno + "]ID失敗, ID不存在!");
            return;
        }

        crdIdNo = getValue("ID_NO");
    }

    /*  讀取商務卡自然人卡號  */
    private void selectCardNo() throws Exception {
        sqlCmd = " SELECT CARD_NO " +
                " FROM CRD_CARD " +
                " WHERE ACNO_P_SEQNO = ?";
        setString(1, actAcnoPSeqno);

        selectTable();
        if ("Y".equals(notFound)) {
            showLogMessage("I", "", "ERROR : 讀取CRD_CARD[ACNO_P_SEQNO=" + actIdPSeqno + "]卡號失敗, 卡號不存在!");
            return;
        }

        crdCardNo = getValue("CARD_NO");
    }

    /*  編列銷帳編號  */
    private void compileChargeOffNum() {
        try {
            if ("".equals(crdIdNo)) {
                return;
            }

            // 前3位
            if (crdIdNo.substring(0, 2).matches("^[a-zA-Z]{2}$")) {
                actPaymentNo += "803";
            } else if (crdIdNo.substring(8, 10).matches("^[a-zA-Z]{2}$")) {
                actPaymentNo += "802";
            } else if (crdIdNo.substring(0, 1).matches("^[a-zA-Z]$")) {
                actPaymentNo += "801";
            } else {
                showLogMessage("I", "", "ERROR : 編列銷帳編號失敗, CRD_IDNO卡號[ID_NO=" + crdIdNo + "]類別無效!");
                return;
            }

            // 第4位
            actPaymentNo += crdIdNo.substring(2, 3);
            // 第5位
            actPaymentNo += crdIdNo.substring(4, 5);
            // 第6位
            actPaymentNo += crdIdNo.substring(6, 7);
            // 第7位
            actPaymentNo += (Integer.parseInt(crdIdNo.substring(2, 3))
                    + Integer.parseInt(crdIdNo.substring(4, 5))
                    + Integer.parseInt(crdIdNo.substring(6, 7)))
                    % 10;
            // 第8位
            actPaymentNo += crdIdNo.substring(8, 9);
            // 第9位
            actPaymentNo += crdIdNo.substring(0, 1);
            // 第10位
            actPaymentNo += crdIdNo.substring(1, 2);
            // 第11位
            actPaymentNo += crdIdNo.substring(2, 3);
            // 第12位
            actPaymentNo += 9 - Integer.parseInt(crdIdNo.substring(3, 4));
            // 第13位
            actPaymentNo += 9 - Integer.parseInt(crdIdNo.substring(5, 6));
            // 第14位
            actPaymentNo += 9 - Integer.parseInt(crdIdNo.substring(7, 8));
            // 第15位
            actPaymentNo += ((9 - Integer.parseInt(crdIdNo.substring(3, 4)))
                    + (9 - Integer.parseInt(crdIdNo.substring(5, 6)))
                    + (9 - Integer.parseInt(crdIdNo.substring(7, 8))))
                    % 10;
            // 第16位
            actPaymentNo += crdIdNo.substring(9, 10);
        } catch (Exception exception) {
            actPaymentNo = "";
            showLogMessage("I", "", "ERROR : 編列銷帳編號失敗, CRD_IDNO卡號[ID_NO=" + crdIdNo + "]格式錯誤: " + comc.getStackTraceString(exception));
        }
    }

    /*  編列虛擬編號  */
    private void compileVirtualSerialNum() throws Exception {
        // 一般卡
        if (crdIdNo != null && crdIdNo.length() > 0) {
            try {
                // 新外國自然人ID
                if (crdIdNo.substring(0, 2).matches("^[a-zA-Z]{2}$")) {
                    actPaymentNoII += "9967";
                    actPaymentNoII += letterToInt(crdIdNo.substring(0, 2));
                    actPaymentNoII += crdIdNo.substring(2, 10);
                }
                // 一般本國自然人ID
                else if (crdIdNo.substring(0, 1).matches("^[a-zA-Z]$")) {
                    actPaymentNoII += "99666";
                    actPaymentNoII += letterToInt(crdIdNo.substring(0, 1));
                    actPaymentNoII += crdIdNo.substring(1, 10);
                }
                // 一般外國自然人ID
                else if (crdIdNo.substring(8, 10).matches("^[a-zA-Z]{2}$")) {
                    actPaymentNoII += "9965";
                    actPaymentNoII += crdIdNo.substring(0, 8);
                    actPaymentNoII += letterToInt(crdIdNo.substring(8, 10));
                }
//                // 統一編號
//                else if (crdIdNo.matches("^[0-9]{8}$")) {
//                    actPaymentNoII += "99666000";
//                    actPaymentNoII += crdIdNo;
//                }
//                // 行政機關編號
//                else if (crdIdNo.substring(9, 10).matches("^[a-zA-Z]$")) {
//                    actPaymentNoII += "99667";
//                    actPaymentNoII += crdIdNo.substring(0, 9);
//                    actPaymentNoII += letterToInt(crdIdNo.substring(9, 10));
//                }
                // 帳號類別無效
                else {
                    showLogMessage("I", "", "ERROR : 編列虛擬編號失敗, CRD_IDNO帳號[ID_NO=" + crdIdNo + "]類別無效!");
                }
            } catch (Exception exception) {
                actPaymentNoII = "";
                showLogMessage("I", "", "ERROR : 編列虛擬編號失敗, CRD_IDNO帳號[ID_NO=" + crdIdNo + "]格式錯誤: " + comc.getStackTraceString(exception));
            }
        }
        // 商務卡個繳戶
        else if (crdCardNo != null && crdCardNo.length() > 0) {
            try {
                int firstNineDigits = Integer.parseInt(crdCardNo.substring(0, 9));

                // 政府採購卡及商務卡
                if (firstNineDigits >= 540520030 && firstNineDigits <= 540520039) {
                    actPaymentNoII += "9960";
                } else if (firstNineDigits == 540970999) {
                    actPaymentNoII += "9961";
                } else if (firstNineDigits >= 540520020 && firstNineDigits <= 540520029) {
                    actPaymentNoII += "9962";
                } else if (firstNineDigits >= 540520010 && firstNineDigits <= 540520019) {
                    actPaymentNoII += "9963";
                } else if (firstNineDigits >= 540520000 && firstNineDigits <= 540520009) {
                    actPaymentNoII += "9964";
                }
                // 採購融資卡
                else if (firstNineDigits >= 486605000 && firstNineDigits <= 486605009) {
                    actPaymentNoII += "9959";
                } else if (firstNineDigits == 540970099) {
                    actPaymentNoII += "9958";
                }
                // 台灣菸酒採購卡
                else if (firstNineDigits == 405430003) {
                    actPaymentNoII += "9957";
                }
                // 商務晶片卡
                else if (firstNineDigits >= 540520040 && firstNineDigits <= 540520044) {
                    actPaymentNoII += "9956";
                }
                // 帳號類別無效
                else {
                    showLogMessage("I", "", "ERROR : 編列虛擬編號失敗, CRD_CARD帳號[CARD_NO=" + crdCardNo + "]類別無效!");
                    return;
                }

                actPaymentNoII += crdCardNo.substring(crdCardNo.length() - 8);

                actPaymentNoII += getCheckCode(actPaymentNoII);
            } catch (Exception exception) {
                actPaymentNoII = "";
                showLogMessage("I", "", "ERROR : 編列虛擬編號失敗, CRD_CARD帳號[CARD_NO=" + crdCardNo + "]格式錯誤: " + comc.getStackTraceString(exception));
            }
        }
    }

    /*  更新表數據  */
    private void updateActAcno() throws Exception {
        if ("".equals(actPaymentNo) && "".equals(actPaymentNoII)) {
            noChgDataNum++;
            return;
        }

        String tableName = "ACT_ACNO";
        daoTable = " ACT_ACNO ";

        updateSQL = " PAYMENT_NO = ?, "
                + " PAYMENT_NO_II = ?, "
                + " MOD_USER = ?, "
                + " MOD_PGM = ?, "
                + " MOD_TIME = sysdate ";

        whereStr = " WHERE ACNO_P_SEQNO = ? "
                + " AND ID_P_SEQNO = ? ";

        setString(1, actPaymentNo);
        setString(2, actPaymentNoII);
        setString(3, prgmId);
        setString(4, prgmId);
        setString(5, actAcnoPSeqno);
        setString(6, actIdPSeqno);

        int returnInt = updateTable();
        if (returnInt == 0) {
            log(String.format(" %s Fail To Update [ID_P_SEQNO = %s, ACNO_P_SEQNO = %s]", tableName, actIdPSeqno, actAcnoPSeqno));
        }

        chgDataNum++;
    }

    /*  字母轉數字  */
    private String letterToInt(String letter) throws Exception {
        StringBuilder result = new StringBuilder();

        if (letter != null && letter.length() > 0) {
            for (char c : letter.toUpperCase().toCharArray()) {
                result.append(String.format("%02d", c - 64));
            }
        } else {
            showLogMessage("I", "", "ERROR : 字母[" + letter + "]轉換失敗!");
            throw new Exception("字母長度錯誤");
        }

        return result.toString();
    }

    /*  獲取檢查碼  */
    private String getCheckCode(String code) {
        int result = 0;

        for (int i = 0; i < code.length(); i++) {
            int number = Integer.parseInt(code.substring(i, i + 1));
            result += number * (7 - (i % 6));
        }

        int remainder = result % 11;
        String value = String.valueOf(remainder == 0 ? 1 : 11 - remainder);
        return String.valueOf(value.charAt(value.length() - 1));
    }
}
