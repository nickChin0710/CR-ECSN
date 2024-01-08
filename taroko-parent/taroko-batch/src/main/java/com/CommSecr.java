/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
*  109/07/06  V0.00.00    Zuwei     coding standard, rename field method & format                   *
*  109/07/22  V0.00.01    Zuwei     coding standard, rename field method                   *
*                                                                            *
*****************************************************************************/
package com;

import java.sql.Connection;

import Sec.SecR001;
import Sec.SecR002;

public class CommSecr  extends AccessDAO{
    SecR001 secR001 = new SecR001();
    SecR002 secR002 = new SecR002();
    public CommSecr(Connection conn[], String[] dbAlias) throws Exception {
        // TODO Auto-generated constructor stub
        super.conn = conn;
        setDBalias(dbAlias);
        setSubParm(dbAlias);
        
        secR001.setConnection(conn, dbAlias);
        secR002.setConnection(conn, dbAlias);

        return;
    }
    
 // ******************************************************************************
    public int toEncrypt(String sCmd) {
        int ret;

        showLogMessage("I", "", "SecR001 " + sCmd);
        try {
            String[] newArgs = { sCmd };
            ret = secR001.mainProcess(newArgs);
        } catch (Exception ex) {
            showLogMessage("I", "", "無法執行 SecR001!");
            ret = -1;
        }

        return ret;
    }
    // ********************************************************************
    public int toDecrypt(String sCmd) throws Exception {
        int ret1 = 0;
        String[] opPawd = {""};
        if ((ret1=getITDauth(opPawd))!=0) return(ret1);
        
        int ret;

        showLogMessage("I", "", "SecR002 " + sCmd + " " + opPawd[0]);
        try {
            String[] newArgs = { sCmd, opPawd[0] };
            ret = secR002.mainProcess(newArgs);
        } catch (Exception ex) {
            showLogMessage("I", "", "無法執行 SecR001!");
            ret = -1;
        }

        return ret;
    }

    /*****************************************************************************/
    int getITDauth(String[] opPawd) throws Exception {
        String hFepdPawdOp = "";

        sqlCmd = "select passwd_op from   sec_file_passwd ";
        try {
            selectTable();
        } catch (Exception ex) {
            showLogMessage("I", "", "select sec_file_passwd error !");
            return(1);
        }

        if (notFound.equals("Y")) {
            showLogMessage("I", "", "檔案加密資訊主管密碼尚未開戶 !");
            return(2);
        }

        hFepdPawdOp = getValue("passwd_op");
        opPawd[0] = "";
        String tempStr = hFepdPawdOp;
        showLogMessage("I", "", "[DEBUG] comcr.h_fepd_pawd_op = "+ hFepdPawdOp);
        String tmpStr1 = "";
        for (int int1 = 0; int1 < hFepdPawdOp.length(); int1++) {
            String tmpD = String.format("%03d", Integer.valueOf((Byte.toString(tempStr.substring(int1, int1 + 1).getBytes()[0]))));
            tmpStr1 += tmpD;
        }
        opPawd[0] = tmpStr1;

        return(0);
    }
}
