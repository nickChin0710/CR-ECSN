package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bank.authbatch.main.AuthBatchDbHandler;
import bank.authbatch.main.HpeUtil;

import bank.authbatch.vo.CcaOutgoingVo;
import bank.authbatch.vo.Data004Vo;

public class CcaOutgoingDao extends AuthBatchDbHandler {

    public CcaOutgoingDao() throws Exception {
        // TODO Auto-generated constructor stub
    }

    public static boolean insertOutGoing(int nP_Opt, int nP_Type, ResultSet P_OnBatRs, ResultSet P_CardInfoRs,
            String sP_DbNegOppReason, String sP_DbNegCapCode, String sP_DbJcbExcpCode, String sP_DbMstAuthCode,
            String sP_DbVisExcepCode, String sP_OtActCode, String sP_ProgName) throws Exception {
        // proc is Gen_IsoDetailString()

        boolean bL_Result = true;
        String sL_CurDate = HpeUtil.getCurDateStr("");
        String sL_CardNewEndDate = P_CardInfoRs.getString("CardNewEndDate");
        String sL_NegDelDate = HpeUtil.getNextMonthDate(sL_CardNewEndDate);

        String sL_CardNo = P_OnBatRs.getString("OnBatCardNo");

        String sL_IsoMsgHeader = "ISO086000051";
        String sL_IsoMsgType = "0300";

        String sL_IsoField2 = "016" + sL_CardNo;
        if (sL_CardNo.substring(0, 4).equals("4000"))
            sL_IsoField2 = "016" + "9" + sL_CardNo.substring(1, sL_CardNo.length());

        String sL_IsoField7 = HpeUtil.getCurDateStr("").substring(4, 8) + HpeUtil.getCurTimeStr();

        String sL_SeqVal = SystemDao.getNextSeqVal("ECS_TRACE_NO");

        String sL_IsoField11 = HpeUtil.fillCharOnLeft(sL_SeqVal, 6, "0");

        String sL_IsoField48 = "076" + "000BK0231" + "00000000000000000000" + "00000000000000000000"
                + "00000000000000000000" + "0000000";
        String sL_IsoField49 = "901";
        String sL_IsoField60 = "058" + "9002" + "0000PRO200000000000000YY000000000000000000000000000000";

        String sL_CardCardType = P_CardInfoRs.getString("CardCardType");
        String sL_CardBinType = P_CardInfoRs.getString("CardBinType");

        // down, process field 73
        String sL_Tmp = "";
        if ("J".equals(sL_CardBinType))
            sL_Tmp = "000000";
        else
            sL_Tmp = sL_NegDelDate;
        String sL_IsoField73 = sL_Tmp;
        // up, process field 73

        // down, process field 91
        String sL_IsoField91 = "1"; // add file
        if (nP_Type == 2) { // delete file
            sL_IsoField91 = "3";// delete file
            if (("J".equals(sL_CardBinType)) && (nP_Opt != 1)) {
                sL_IsoField91 = "0";
            }
        }
        // up, process field 91

        String sL_IsoField101 = "";
        String sL_IsoField120 = "";
        if (nP_Opt == 1) {
            sL_IsoField101 = "02" + "NF";

            if (nP_Type == 1) {/** add(凍結) 才需待出以下資料 **/
                // down, process field 120
                sL_IsoField120 = "015" + HpeUtil.fillCharOnRight(sL_CardCardType, 2, " ");
                sL_IsoField120 += sP_DbNegOppReason + sP_DbNegCapCode;
                sL_IsoField120 += sL_CurDate.substring(2, 8) + sL_NegDelDate.substring(2, 6);
                // up, process field 120
            }
        } else {/* Outgoing VISA or Master or JCB */
            if ("J".equals(sL_CardBinType)) { // JCB Exception File
                sL_IsoField101 = "04" + "6332";

                // down, process field 120
                sL_IsoField120 = "042" + "PATH" + sL_CardNo.substring(0, 6);

                if (nP_Type == 1)
                    sL_IsoField120 += "301"; /* 1 for Add 2 for update */
                else
                    sL_IsoField120 += "170"; /* for delete */
                sL_IsoField120 += sL_CardNo;

                if (nP_Type == 1) {
                    sL_IsoField120 += sP_DbJcbExcpCode;
                    sL_IsoField120 += sL_NegDelDate.substring(2, 6);
                    sL_IsoField120 += "00000";/* JCB region code */
                } else {
                    sL_IsoField120 += "             "; /* 13 spaces */
                }
                // up, process field 120

            } else if ("M".equals(sL_CardBinType)) {
                sL_IsoField101 = "02" + "MC";
                // down, process field 120
                if (nP_Type == 1)
                    sL_IsoField120 = "0092"; // Howard: 新的spec應該是 "0092", old proc is "0075"
                else
                    sL_IsoField120 = "0047";

                sL_IsoField120 += "PATHMCC";
                if (nP_Type == 1)
                    sL_IsoField120 += "1"; /* 1 for Add 2 for update */
                else
                    sL_IsoField120 += "3"; /* for delete */
                sL_IsoField120 += "MCC102         " + "00000";
                sL_IsoField120 += sL_CardNo + "   ";

                if (nP_Type == 1) { /* add data need to send */
                    sL_IsoField120 += sP_DbMstAuthCode;
                    sL_IsoField120 += "000000000000";
                    sL_IsoField120 += "000000000000";
                    sL_IsoField120 += "   ";
                    sL_IsoField120 += "        ";// "PurgeDate(YYYYMMDD)";
                    sL_IsoField120 += "   ";// "CardSeqNo(3 bytes)";
                    sL_IsoField120 += sL_CardNewEndDate.substring(0, 6);// expire date (YYYYMM)

                }
                // up, process field 120
            } else { // for VISA card (VISA Exception File)
                sL_IsoField101 = "02" + "VP";
                // down, process field 120
                sL_IsoField120 = "050" + "PCAS";
                if (nP_Type == 1)
                    sL_IsoField120 += "BA"; /* for add or update */
                else
                    sL_IsoField120 += "BD"; /* for delete */

                sL_IsoField120 += HpeUtil.fillCharOnRight(sL_CardNo, 28, " ");
                sL_IsoField120 += "   "; /* 3 Byte Country code */
                sL_IsoField120 += "  ";/* 2 Byte Operator ID */

                if (sP_DbVisExcepCode.length() == 0) {
                    sL_IsoField120 += "05";

                } else {
                    sL_IsoField120 += sP_DbVisExcepCode;
                }
                sL_IsoField120 += "0        "; /* 9 Byte Region code */
                // up, process field 120
            }
        }

        // down, insert into table outgoing
        CcaOutgoingVo L_OutgoingVo = new CcaOutgoingVo();
        L_OutgoingVo.setActCode(sP_OtActCode); // 作業碼
        L_OutgoingVo.setCardNo(sL_CardBinType);
        L_OutgoingVo.setCrtUser("ECS004");
        L_OutgoingVo.setIsoField002(sL_IsoField2);
        L_OutgoingVo.setIsoField007(sL_IsoField7);
        L_OutgoingVo.setIsoField011(sL_IsoField11);
        L_OutgoingVo.setIsoField048(sL_IsoField48);
        L_OutgoingVo.setIsoField049(sL_IsoField49);
        L_OutgoingVo.setIsoField060(sL_IsoField60);
        L_OutgoingVo.setIsoField073(sL_IsoField73);
        L_OutgoingVo.setIsoField091(sL_IsoField91);
        L_OutgoingVo.setIsoField101(sL_IsoField101);
        L_OutgoingVo.setIsoField120(sL_IsoField120);

        L_OutgoingVo.setMsgHeader(sL_IsoMsgHeader);
        L_OutgoingVo.setMsgType(sL_IsoMsgType);
        L_OutgoingVo.setModPgm(sP_ProgName);

        L_OutgoingVo.setModTime(HpeUtil.getCurTimestamp());
        L_OutgoingVo.setProcDate("");
        L_OutgoingVo.setProcTime("");
        L_OutgoingVo.setProcFlag("N");
        L_OutgoingVo.setProcUser(sP_ProgName);
        L_OutgoingVo.setSendTimes(0);

        CcaOutgoingDao.insertOutgoing(L_OutgoingVo);
        // up, insert into table outgoing

        return bL_Result;

    }

    public static boolean insertOutGoing(int nP_Opt, int nP_Type, Data004Vo P_Data004Vo, ResultSet P_CardInfoRs,
            String sP_DbNegOppReason, String sP_DbNegCapCode, String sP_DbJcbExcpCode, String sP_DbMstAuthCode,
            String sP_DbVisExcepCode, String sP_OtActCode, String sP_ProgName) throws Exception {
        // proc is Gen_IsoDetailString()

        boolean bL_Result = true;
        String sL_CurDate = HpeUtil.getCurDateStr("");
        String sL_CardNewEndDate = P_CardInfoRs.getString("CardNewEndDate");
        String sL_NegDelDate = HpeUtil.getNextMonthDate(sL_CardNewEndDate);

        String sL_CardNo = P_Data004Vo.getCardNo();

        String sL_IsoMsgHeader = "ISO086000051";
        String sL_IsoMsgType = "0300";

        String sL_IsoField2 = "016" + sL_CardNo;
        if (sL_CardNo.substring(0, 4).equals("4000"))
            sL_IsoField2 = "016" + "9" + sL_CardNo.substring(1, sL_CardNo.length());

        String sL_IsoField7 = HpeUtil.getCurDateStr("").substring(4, 8) + HpeUtil.getCurTimeStr();

        String sL_SeqVal = SystemDao.getNextSeqVal("ECS_TRACE_NO");

        String sL_IsoField11 = HpeUtil.fillCharOnLeft(sL_SeqVal, 6, "0");

        String sL_IsoField48 = "076" + "000BK0231" + "00000000000000000000" + "00000000000000000000"
                + "00000000000000000000" + "0000000";
        String sL_IsoField49 = "901";
        String sL_IsoField60 = "058" + "9002" + "0000PRO200000000000000YY000000000000000000000000000000";

        String sL_CardCardType = P_CardInfoRs.getString("CardCardType");
        String sL_CardBinType = P_CardInfoRs.getString("CardBinType");

        // down, process field 73
        String sL_Tmp = "";
        if ("J".equals(sL_CardBinType))
            sL_Tmp = "000000";
        else
            sL_Tmp = sL_NegDelDate;
        String sL_IsoField73 = sL_Tmp;
        // up, process field 73

        // down, process field 91
        String sL_IsoField91 = "1"; // add file
        if (nP_Type == 2) { // delete file
            sL_IsoField91 = "3";// delete file
            if (("J".equals(sL_CardBinType)) && (nP_Opt != 1)) {
                sL_IsoField91 = "0";
            }
        }
        // up, process field 91

        String sL_IsoField101 = "";
        String sL_IsoField120 = "";
        if (nP_Opt == 1) {
            sL_IsoField101 = "02" + "NF";

            if (nP_Type == 1) {/** add(凍結) 才需待出以下資料 **/
                // down, process field 120
                sL_IsoField120 = "015" + HpeUtil.fillCharOnRight(sL_CardCardType, 2, " ");
                sL_IsoField120 += sP_DbNegOppReason + sP_DbNegCapCode;
                sL_IsoField120 += sL_CurDate.substring(2, 8) + sL_NegDelDate.substring(2, 6);
                // up, process field 120
            }
        } else {/* Outgoing VISA or Master or JCB */
            if ("J".equals(sL_CardBinType)) { // JCB Exception File
                sL_IsoField101 = "04" + "6332";

                // down, process field 120
                sL_IsoField120 = "042" + "PATH" + sL_CardNo.substring(0, 6);

                if (nP_Type == 1)
                    sL_IsoField120 += "301"; /* 1 for Add 2 for update */
                else
                    sL_IsoField120 += "170"; /* for delete */
                sL_IsoField120 += sL_CardNo;

                if (nP_Type == 1) {
                    sL_IsoField120 += sP_DbJcbExcpCode;
                    sL_IsoField120 += sL_NegDelDate.substring(2, 6);
                    sL_IsoField120 += "00000";/* JCB region code */
                } else {
                    sL_IsoField120 += "             "; /* 13 spaces */
                }
                // up, process field 120

            } else if ("M".equals(sL_CardBinType)) {
                sL_IsoField101 = "02" + "MC";
                // down, process field 120
                if (nP_Type == 1)
                    sL_IsoField120 = "0092"; // Howard: 新的spec應該是 "0092", old proc is "0075"
                else
                    sL_IsoField120 = "0047";

                sL_IsoField120 += "PATHMCC";
                if (nP_Type == 1)
                    sL_IsoField120 += "1"; /* 1 for Add 2 for update */
                else
                    sL_IsoField120 += "3"; /* for delete */
                sL_IsoField120 += "MCC102         " + "00000";
                sL_IsoField120 += sL_CardNo + "   ";

                if (nP_Type == 1) { /* add data need to send */
                    sL_IsoField120 += sP_DbMstAuthCode;
                    sL_IsoField120 += "000000000000";
                    sL_IsoField120 += "000000000000";
                    sL_IsoField120 += "   ";
                    sL_IsoField120 += "        ";// "PurgeDate(YYYYMMDD)";
                    sL_IsoField120 += "   ";// "CardSeqNo(3 bytes)";
                    sL_IsoField120 += sL_CardNewEndDate.substring(0, 6);// expire date (YYYYMM)

                }
                // up, process field 120
            } else { // for VISA card (VISA Exception File)
                sL_IsoField101 = "02" + "VP";
                // down, process field 120
                sL_IsoField120 = "050" + "PCAS";
                if (nP_Type == 1)
                    sL_IsoField120 += "BA"; /* for add or update */
                else
                    sL_IsoField120 += "BD"; /* for delete */

                sL_IsoField120 += HpeUtil.fillCharOnRight(sL_CardNo, 28, " ");
                sL_IsoField120 += "   "; /* 3 Byte Country code */
                sL_IsoField120 += "  ";/* 2 Byte Operator ID */

                if (sP_DbVisExcepCode.length() == 0) {
                    sL_IsoField120 += "05";

                } else {
                    sL_IsoField120 += sP_DbVisExcepCode;
                }
                sL_IsoField120 += "0        "; /* 9 Byte Region code */
                // up, process field 120
            }
        }

        // down, insert into table outgoing
        CcaOutgoingVo L_OutgoingVo = new CcaOutgoingVo();
        L_OutgoingVo.setActCode(sP_OtActCode); // 作業碼
        L_OutgoingVo.setCardNo(sL_CardBinType);
        L_OutgoingVo.setCrtUser("ECS004");
        L_OutgoingVo.setIsoField002(sL_IsoField2);
        L_OutgoingVo.setIsoField007(sL_IsoField7);
        L_OutgoingVo.setIsoField011(sL_IsoField11);
        L_OutgoingVo.setIsoField048(sL_IsoField48);
        L_OutgoingVo.setIsoField049(sL_IsoField49);
        L_OutgoingVo.setIsoField060(sL_IsoField60);
        L_OutgoingVo.setIsoField073(sL_IsoField73);
        L_OutgoingVo.setIsoField091(sL_IsoField91);
        L_OutgoingVo.setIsoField101(sL_IsoField101);
        L_OutgoingVo.setIsoField120(sL_IsoField120);

        L_OutgoingVo.setMsgHeader(sL_IsoMsgHeader);
        L_OutgoingVo.setMsgType(sL_IsoMsgType);
        L_OutgoingVo.setModPgm(sP_ProgName);

        L_OutgoingVo.setModTime(HpeUtil.getCurTimestamp());
        L_OutgoingVo.setProcDate("");
        L_OutgoingVo.setProcTime("");
        L_OutgoingVo.setProcFlag("N");
        L_OutgoingVo.setProcUser(sP_ProgName);
        L_OutgoingVo.setSendTimes(0);

        CcaOutgoingDao.insertOutgoing(L_OutgoingVo);
        // up, insert into table outgoing

        return bL_Result;

    }

    public static boolean insertOutgoing(CcaOutgoingVo P_OutgoingVo) {
        boolean bL_Result = true;

        try {
            String sL_Sql = "";
            sL_Sql = "insert into CCA_OUTGOING(MSG_HEADER,MSG_TYPE,ISOFIELD_002,ISOFIELD_007,ISOFIELD_011,ISOFIELD_048,ISOFIELD_049,ISOFIELD_060,ISOFIELD_073,ISOFIELD_091,ISOFIELD_101,ISOFIELD_120,CARD_NO,PROC_FLAG ,SEND_TIMES ,ACT_CODE  ,CRT_USER, MOD_TIME, MOD_PGM) ";
            // sL_Sql += "values(:p1, :p2,:p3
            // ,:p4,:p5,:p6,:p7,:p8,:p9,:p10,:p11,:p12,:pCardNo ,:p14,:p15,:p16,:p17,:p18,
            // :p19 )";
            sL_Sql += "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            // NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
            PreparedStatement ps = getPreparedStatement(sL_Sql, true);
            ps.setString(1, P_OutgoingVo.getMsgHeader());
            ps.setString(2, P_OutgoingVo.getMsgType());
            ps.setString(3, P_OutgoingVo.getIsoField002());
            ps.setString(4, P_OutgoingVo.getIsoField007());
            ps.setString(5, P_OutgoingVo.getIsoField011());
            ps.setString(6, P_OutgoingVo.getIsoField048());
            ps.setString(7, P_OutgoingVo.getIsoField049());
            ps.setString(8, P_OutgoingVo.getIsoField060());
            ps.setString(9, P_OutgoingVo.getIsoField073());
            ps.setString(10, P_OutgoingVo.getIsoField091());
            ps.setString(11, P_OutgoingVo.getIsoField101());
            ps.setString(12, P_OutgoingVo.getIsoField120());

            ps.setString(13, P_OutgoingVo.getCardNo());
            ps.setString(14, P_OutgoingVo.getProcFlag());
            ps.setInt(15, P_OutgoingVo.getSendTimes());
            ps.setString(16, P_OutgoingVo.getActCode());
            ps.setString(17, P_OutgoingVo.getCrtUser());
            ps.setTimestamp(18, P_OutgoingVo.getModTime());
            ps.setString(19, P_OutgoingVo.getModPgm());

            ps.executeUpdate();
            ps.close();

        } catch (Exception e) {
            // TODO: handle exception
            bL_Result = false;
        }
        return bL_Result;
    }

    public static boolean updateOutgoing(String sP_MsgHeader, String sP_MsgType, String sP_CardNo) {
        boolean bL_Result = true;

        try {
            String sL_Sql = "";
            sL_Sql = "update CCA_OUTGOING set MSG_HEADER= ? ,MSG_TYPE= ? where CARD_NO= ? ";
            // NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
            PreparedStatement ps = getPreparedStatement(sL_Sql, false);
            ps.setString(1, sP_MsgHeader);
            ps.setString(2, sP_MsgType);

            ps.setString(3, sP_CardNo);
            ps.executeUpdate();
            ps.close();

        } catch (Exception e) {
            // TODO: handle exception
            bL_Result = false;
        }
        return bL_Result;
    }

    public static boolean deleteOutgoing(String sP_CardNo) {
        boolean bL_Result = true;

        try {
            String sL_Sql = "";
            sL_Sql = "delete CCA_OUTGOING where CARD_NO= ?  ";
            // NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
            PreparedStatement ps = getPreparedStatement(sL_Sql, true);

            ps.setString(1, sP_CardNo);
            ps.executeUpdate();
            ps.close();

        } catch (Exception e) {
            // TODO: handle exception
            bL_Result = false;
        }
        return bL_Result;
    }

    public static ResultSet getOutgoing(String sP_CardNo) {

        ResultSet L_ResultSet = null;
        try {
            String sL_Sql = "";
            sL_Sql = "select MSG_HEADER, MSG_TYPE from CCA_OUTGOING where CARD_NO= ? ";
            // NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
            PreparedStatement ps = getPreparedStatement(sL_Sql, true);

            ps.setString(1, sP_CardNo);
            L_ResultSet = ps.executeQuery();

        } catch (Exception e) {
            // TODO: handle exception
            L_ResultSet = null;
        }
        return L_ResultSet;
    }

    public static boolean insertOutgoing(String sP_Sql, String sP_card_no, String sP_key_value, String sP_key_table,
            String sP_bitmap, String sP_sucess_flag, long sP_send_times, String sP_CrtDate, String sP_CrtTime,
            String sP_CrtUser, String sP_ProcDate, String sP_ProcTime, String sP_ProcUser, String sP_act_code) {
        boolean bL_Result = true;

        try {
            // NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
            PreparedStatement ps = getPreparedStatement(sP_Sql, false);
            ps.setString(1, sP_card_no);
            ps.setString(2, sP_key_value);
            ps.setString(3, sP_key_table);
            ps.setString(4, sP_bitmap);
            ps.setString(5, sP_sucess_flag);
            ps.setLong(6, sP_send_times);
            ps.setString(7, sP_CrtDate);
            ps.setString(8, sP_CrtTime);
            ps.setString(9, sP_CrtUser);
            ps.setString(10, sP_ProcDate);
            ps.setString(11, sP_ProcTime);
            ps.setString(12, sP_ProcUser);

            ps.setString(13, sP_act_code);

            ps.executeUpdate();
            ps.close();

        } catch (Exception e) {
            // TODO: handle exception
            bL_Result = false;
        }
        return bL_Result;
    }

}
