/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
 *  109/07/06  V1.00.01    Zuwei     coding standard, rename field method & format                   *
 *  109/07/22  V1.00.02    Zuwei     coding standard, rename variable                   *
*  109-08-14  V1.00.01  Zuwei      fix code scan issue verify sql��path��ݔ���g�[����ԃ      *
*  110-01-07   V1.00.02    shiyuqi       修改无意义命名                                 
*   111-01-19  V1.00.05    Justin    fix Code Correctness: Constructor Invokes Overridable Function
******************************************************************************/
package com;
 
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import Dxc.Util.SecurityUtil;

public class Big52CNS {

    byte[] tmpCde = new byte[3];
    byte[] cnsSpecial = new byte[3];

    byte[] big5C1 = { (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1,
            (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1,
            (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1,
            (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1,
            (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1,
            (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1,
            (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1,
            (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1,
            (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1,
            (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1,
            (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1, (byte) 0xA2, (byte) 0xA2 };
    byte[] big5C2 = { (byte) 0x40, (byte) 0x41, (byte) 0x42, (byte) 0x43, (byte) 0x44, (byte) 0x45, (byte) 0x46,
            (byte) 0x47, (byte) 0x48, (byte) 0x49, (byte) 0x4A, (byte) 0x4B, (byte) 0x4C, (byte) 0x4D, (byte) 0x4E,
            (byte) 0x4F, (byte) 0x50, (byte) 0x51, (byte) 0x52, (byte) 0x53, (byte) 0x54, (byte) 0x55, (byte) 0x56,
            (byte) 0x57, (byte) 0x58, (byte) 0x59, (byte) 0x5A, (byte) 0x5B, (byte) 0x5C, (byte) 0x5D, (byte) 0x5E,
            (byte) 0x5F, (byte) 0x60, (byte) 0x61, (byte) 0x62, (byte) 0x63, (byte) 0x64, (byte) 0x65, (byte) 0x66,
            (byte) 0x67, (byte) 0x68, (byte) 0x69, (byte) 0x6A, (byte) 0x6B, (byte) 0x6C, (byte) 0x6D, (byte) 0x6E,
            (byte) 0x6F, (byte) 0x70, (byte) 0x71, (byte) 0x72, (byte) 0x73, (byte) 0x74, (byte) 0x75, (byte) 0x76,
            (byte) 0x77, (byte) 0x78, (byte) 0x79, (byte) 0x7A, (byte) 0x7B, (byte) 0x7C, (byte) 0x7D, (byte) 0x7E,
            (byte) 0xA1, (byte) 0xA2, (byte) 0xA3, (byte) 0xA4, (byte) 0xA5, (byte) 0xA6, (byte) 0xA7, (byte) 0xA8,
            (byte) 0xA9, (byte) 0xAA, (byte) 0xAB, (byte) 0xAC, (byte) 0xAF, (byte) 0xAD, (byte) 0xAE, (byte) 0xCF,
            (byte) 0xD0, (byte) 0xD1, (byte) 0xD7, (byte) 0xD5, (byte) 0xD6, (byte) 0x43, (byte) 0x48 };
    byte[] cnsC1 = { (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21,
            (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21,
            (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21,
            (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21,
            (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21,
            (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21,
            (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21,
            (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21,
            (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21,
            (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x21, (byte) 0x22,
            (byte) 0x22, (byte) 0x22, (byte) 0x22, (byte) 0x22, (byte) 0x22, (byte) 0x22, (byte) 0x22 };
    byte[] cnsC2 = { (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26, (byte) 0x27,
            (byte) 0x28, (byte) 0x29, (byte) 0x2A, (byte) 0x2B, (byte) 0x2C, (byte) 0x2D, (byte) 0x2E, (byte) 0x2F,
            (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37,
            (byte) 0x38, (byte) 0x39, (byte) 0x3A, (byte) 0x3B, (byte) 0x3C, (byte) 0x3D, (byte) 0x3E, (byte) 0x3F,
            (byte) 0x40, (byte) 0x41, (byte) 0x42, (byte) 0x43, (byte) 0x44, (byte) 0x45, (byte) 0x46, (byte) 0x47,
            (byte) 0x48, (byte) 0x49, (byte) 0x4A, (byte) 0x4B, (byte) 0x4C, (byte) 0x4D, (byte) 0x4E, (byte) 0x4F,
            (byte) 0x50, (byte) 0x51, (byte) 0x52, (byte) 0x53, (byte) 0x54, (byte) 0x55, (byte) 0x56, (byte) 0x57,
            (byte) 0x58, (byte) 0x59, (byte) 0x5A, (byte) 0x5B, (byte) 0x5C, (byte) 0x5D, (byte) 0x5E, (byte) 0x5F,
            (byte) 0x60, (byte) 0x61, (byte) 0x62, (byte) 0x63, (byte) 0x64, (byte) 0x65, (byte) 0x66, (byte) 0x67,
            (byte) 0x68, (byte) 0x69, (byte) 0x6A, (byte) 0x6B, (byte) 0x6E, (byte) 0x6C, (byte) 0x6D, (byte) 0x30,
            (byte) 0x31, (byte) 0x32, (byte) 0x38, (byte) 0x36, (byte) 0x37, (byte) 0x63, (byte) 0x68 };

    private byte[] cnsBuf = new byte[66000];
    DetlCns detlCns = new DetlCns();

    public Big52CNS() throws IOException {
        loadCnsTable();
    }

    public CnsResult convCns(byte[] bigStr) throws IOException {
        return convCns(bigStr, 501);
    }
    public CnsResult convCns(byte[] bigStr, int inputLen) throws IOException {
        byte[] cnsStr = new byte[inputLen];
        int i, j, int1, no, sr;
        byte con1, con2;
        int param, bigLow, bigHigh, speLow, speHigh;
        byte[] con = new byte[3];
        byte[] cnsSpace = new byte[3];
        byte wsChin;

        bigLow = 0xa440;
        bigHigh = 0xf9d5;
        speLow = 0xa2af;
        speHigh = 0xa2e8;

        cnsSpace[0] = 0x21;
        cnsSpace[1] = (byte) 0xa1;

        no = bigStr.length;
        if (no > 500) {
            return null;
        }
        i = 0;
        j = 0;
        wsChin = 'N';

        while (true) {
            if (i >= no) {
                if (wsChin == 'Y') {
                    cnsStr[j] = 0x0F;
                }
                break;
            }

            if ((bigStr[i] & 0x80) == 0) {
                if (wsChin == 'Y') {
                    cnsStr[j] = 0x0F;
                    j++;
                    wsChin = 'N';
                }
                cnsStr[j] = bigStr[i];
                i++;
                j++;
                continue;
            }

            if (wsChin != 'Y') {
                cnsStr[j] = 0x0E;
                j++;
            }

            wsChin = 'Y';

            int1 = i;
            con[0] = bigStr[int1];
            con[1] = bigStr[++int1];
            con1 = con[0];
            con2 = con[1];
            param = (byteToUnsignedInt(con1) * 256 + byteToUnsignedInt(con2));

            if ((param >= speLow && param <= speHigh) || (param >= 0xa140 && param <= 0xa1d7) || (param >= 0xa243 && param <= 0xa248)) {
                sr = i;
                tmpCde[0] = bigStr[sr];
                tmpCde[1] = bigStr[++sr];
                specialCode();
                System.arraycopy(cnsSpecial, 0, cnsStr, j, 2);
                i = i + 2;
                j = j + 2;
                continue;
            }

            if (param < bigLow || param > bigHigh) {
                System.arraycopy(cnsSpace, 0, cnsStr, j, 2);
                i = i + 2;
                j = j + 2;
                continue;
            }

            if (param >= bigLow && param <= bigHigh) {
                param = (param - bigLow);
            }

            if (detlCns.indx[param] == 0x02) {
                cnsStr[j] = 0x1B;
                j++;
                cnsStr[j] = 0x4E;
                j++;
            } else if (detlCns.indx[param] == 0x03) {
                cnsStr[j] = 0x1B;
                j++;
                cnsStr[j] = 0x4F;
                j++;
            }

            cnsStr[j] = detlCns.code1[param];
            cnsStr[j + 1] = detlCns.code2[param];

            i = i + 2;
            j = j + 2;
        }
        CnsResult rtn = new CnsResult();
        rtn.data = new byte[inputLen];//501
        rtn.length = j + 1;
        System.arraycopy(cnsStr, 0, rtn.data, 0, inputLen);//501
        return rtn;
    }

    // fix Code Correctness: Constructor Invokes Overridable Function
    private final void loadCnsTable() throws IOException {
        String tmpstr;
        long rcount;
        int fcnt, rfd;

        //tmpstr = String.format("%s/etc/BIG2CNS.DAT", comc.GetECSHOME());
        tmpstr = String.format("%s/etc/BIG2CNS.DAT", System.getenv("PROJ_HOME")); // for
                                                                                  // test

        rcount = 66000;
     // verify path string
        String tempPath = SecurityUtil.verifyPath(tmpstr);
        //2020_0615 resolve Unreleased Resource: Streams by yanghan
        try(BufferedInputStream br = new BufferedInputStream(new FileInputStream(tempPath))){
          fcnt = br.read(cnsBuf, 0, 66000);
          if (fcnt != rcount) {
              System.out.println(" READ BIG2CNS.DAT ERROR !! ");
              throw new IOException("READ BIG2CNS.DAT ERROR");
          }
          br.close();
        }
        
        int j = 0;
        for (int i = 0; i < 66000; i = i + 3) {
            byte[] tmpBytes = new byte[3];
            System.arraycopy(cnsBuf, i, tmpBytes, 0, 3);
            detlCns.indx[j] = tmpBytes[0];
            detlCns.code1[j] = tmpBytes[1];
            detlCns.code2[j] = tmpBytes[2];

            j++;
        }
        return;
    }

    void specialCode() {
        int lcnt;

        cnsSpecial[0] = ' ';
        cnsSpecial[1] = ' ';

        if (tmpCde[0] == 0xA2 && tmpCde[1] == 0xAF) {
            cnsSpecial[0] = 0x24;
            cnsSpecial[1] = 0x21;
            return;
        }

        if (tmpCde[0] == 0xA2 && tmpCde[1] == 0xB0) {
            cnsSpecial[0] = 0x24;
            cnsSpecial[1] = 0x22;
            return;
        }

        if (tmpCde[0] == 0xA2 && tmpCde[1] == 0xB1) {
            cnsSpecial[0] = 0x24;
            cnsSpecial[1] = 0x23;
            return;
        }

        if (tmpCde[0] == 0xA2 && tmpCde[1] == 0xB2) {
            cnsSpecial[0] = 0x24;
            cnsSpecial[1] = 0x24;
            return;
        }

        if (tmpCde[0] == 0xA2 && tmpCde[1] == 0xB3) {
            cnsSpecial[0] = 0x24;
            cnsSpecial[1] = 0x25;
            return;
        }

        if (tmpCde[0] == 0xA2 && tmpCde[1] == 0xB4) {
            cnsSpecial[0] = 0x24;
            cnsSpecial[1] = 0x26;
            return;
        }

        if (tmpCde[0] == 0xA2 && tmpCde[1] == 0xB5) {
            cnsSpecial[0] = 0x24;
            cnsSpecial[1] = 0x27;
            return;
        }

        if (tmpCde[0] == 0xA2 && tmpCde[1] == 0xB6) {
            cnsSpecial[0] = 0x24;
            cnsSpecial[1] = 0x28;
            return;
        }

        if (tmpCde[0] == 0xA2 && tmpCde[1] == 0xB7) {
            cnsSpecial[0] = 0x24;
            cnsSpecial[1] = 0x29;
            return;
        }

        if (tmpCde[0] == 0xA2 && tmpCde[1] == 0xB8) {
            cnsSpecial[0] = 0x24;
            cnsSpecial[1] = 0x2A;
            return;
        }

        for (lcnt = 0; lcnt < 86; lcnt++)
            if (tmpCde[0] == big5C1[lcnt] && tmpCde[1] == big5C2[lcnt]) {
                cnsSpecial[0] = cnsC1[lcnt];
                cnsSpecial[1] = cnsC2[lcnt];
                return;
            }

        lcnt = 0;
        for (lcnt = 0; lcnt <= 25; lcnt++) {
            if (tmpCde[0] == 0xA2 && tmpCde[1] == 0xCF + lcnt) {
                cnsSpecial[0] = 0x24;
                cnsSpecial[1] = (byte) (0x41 + lcnt);
                return;
            }
        }

        for (lcnt = 0; lcnt <= 25; lcnt++) {
            if (tmpCde[0] == 0xA2 && tmpCde[1] == 0xE9 + lcnt) {
                cnsSpecial[0] = 0x24;
                cnsSpecial[1] = (byte) (0x5B + lcnt);
                return;
            }
        }

        return;
    }

    public int byteToUnsignedInt(byte bytes) {
        return 0x00 << 24 | bytes & 0xff;
    }

    class DetlCns {
        public byte[] indx = new byte[22000];
        public byte[] code1 = new byte[22000];
        public byte[] code2 = new byte[22000];
    }

    public class CnsResult {
        public byte[] data = null;
        public int length = 0;
    }

}
