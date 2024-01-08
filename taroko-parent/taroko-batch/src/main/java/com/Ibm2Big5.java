/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
*  109/07/06  V1.00.00    Zuwei     coding standard, rename field method & format                   *
*  109/07/22  V1.00.01    Zuwei     coding standard, rename field method                   *
*  110-01-07  V1.00.02    shiyuqi   coding standard, rename                     * 
*****************************************************************************/
package com;

import java.io.UnsupportedEncodingException;

public class Ibm2Big5 {
  long val;
  int cnt, wsM1, wsM2, wsN1, wsN2, wsL, lnt;
  byte[] conStr = new byte[1600];
  int ii, kk, j;
  byte ibm1, ibm2;
  int ibmH, ibmL;

  public byte[] ibmBig5(byte[] cvtData, int cLen) throws UnsupportedEncodingException {
    byte[] tmpBytes = new byte[cLen];
    byte[] resultBytes = ibmBig5(cvtData).getBytes("big5");
    System.arraycopy(resultBytes, 0, tmpBytes, 0, resultBytes.length);
    return tmpBytes;

  }

  public String ibmBig5(byte[] cvtData) throws UnsupportedEncodingException {
    /*
     * cnt = 0; for (j = 0; j < c_len; j++) { if (cvt_data[j] == 0x0E) { continue; } if (cvt_data[j]
     * == 0x0F) { break; } con_str[cnt] = cvt_data[j]; cnt++; }
     * 
     * con_str[cnt] = 0x00;
     * 
     * ii = 0;
     * 
     * while (true) { if (ii >= cnt) { break; }
     * 
     * kk = ii; ibm_1 = con_str[ii++]; ibm_2 = con_str[ii++]; val = byteToUnsignedInt(ibm_1) * 256 +
     * byteToUnsignedInt(ibm_2); val += 16383;
     * 
     * if (val >= 35904 && val <= 43209) { phase_1_convert(); } else if (val >= 43328 && val <=
     * 53700) { phase_2_convert(); } else { special_big5(); } }
     * 
     * byte[] rtn = new byte[cnt]; System.arraycopy(con_str, 0, rtn, 0, cnt);
     * 
     * return rtn;
     */

    cnt = 0;
    j = 0;
    conStr[0] = 0x0E;
    cnt++;
    for (j = 0; j < cvtData.length; j++) {
      if (cvtData[j] == 0x0E) {
        continue;
      }
      if (cvtData[j] == 0x0F) {
        break;
      }
      conStr[cnt] = cvtData[j];
      cnt++;
    }
    conStr[cnt] = 0x0F;
    cnt++;
    byte[] rtn = new byte[cnt];
    System.arraycopy(conStr, 0, rtn, 0, cnt);

    return new String(rtn, "Cp937");
  }

  private void phase1Convert() {
    ibmH = (int) (val / 256);
    ibmL = (int) (val % 256);
    wsN2 = ibmH - 140;

    if (ibmL > 126) {
      val = ibmL - 1;
    } else {
      val = ibmL;
    }

    wsM2 = (int) (val - 64);
    wsL = wsN2 * 188 + wsM2;

    wsN1 = wsL / 157;
    wsM1 = wsL % 157;
    val = wsN1 + 164;

    conStr[kk++] = (byte) (val % 256);
    val = wsM1 + 64;
    if (val > 126) {
      val = val - 126 - 1 + 161;
    }

    conStr[kk++] = (byte) (val % 256);

    return;
  }

  private void phase2Convert() {

    ibmH = (int) (val / 256);
    ibmL = (int) (val % 256);
    wsN2 = ibmH - 169;

    if (ibmL > 126) {
      val = ibmL - 1;
    } else {
      val = ibmL;
    }

    wsM2 = (int) (val - 64);
    wsL = wsN2 * 188 + wsM2;

    wsN1 = wsL / 157;
    wsM1 = wsL % 157;
    val = wsN1 + 201;

    conStr[kk++] = (byte) (val % 256);
    val = wsM1 + 64;
    if (val > 126) {
      val = val - 126 - 1 + 161;
    }

    conStr[kk++] = (byte) (val % 256);

    return;
  }

  private void specialBig5() {
    lnt = 0;
    for (lnt = 0; lnt <= 9; lnt++) {
      if (ibm1 == (byte) 0x42 && ibm2 == (byte) (0xF0 + lnt)) {
        conStr[kk++] = (byte) 0xA2;
        conStr[kk++] = (byte) (0xAF + lnt);
        return;
      }
    }

    for (lnt = 0; lnt <= 8; lnt++) {
      if (ibm1 == (byte) 0x42 && ibm2 == (byte) (0xC1 + lnt)) {
        conStr[kk++] = (byte) 0xA2;
        conStr[kk++] = (byte) (0xCF + lnt);
        return;
      }
    }

    for (lnt = 0; lnt <= 8; lnt++) {
      if (ibm1 == (byte) 0x42 && ibm2 == (byte) (0xD1 + lnt)) {
        conStr[kk++] = (byte) 0xA2;
        conStr[kk++] = (byte) (0xD8 + lnt);
        return;
      }
    }

    for (lnt = 0; lnt <= 7; lnt++) {
      if (ibm1 == (byte) 0x42 && ibm2 == (byte) (0xE2 + lnt)) {
        conStr[kk++] = (byte) 0xA2;
        conStr[kk++] = (byte) (0xE1 + lnt);
        return;
      }
    }

    if (ibm1 == (byte) 0x42 && ibm2 == (byte) 0x4B) {
      conStr[kk++] = (byte) 0xA1;
      conStr[kk++] = (byte) 0x5D;
      return;
    }

    if (ibm1 == (byte) 0x42 && ibm2 == (byte) 0x5D) {
      conStr[kk++] = (byte) 0xA1;
      conStr[kk++] = (byte) 0x5E;
      return;
    }

    if (ibm1 == (byte) 0x42 && ibm2 == (byte) 0x60) {
      conStr[kk++] = (byte) 0xA1;
      conStr[kk++] = (byte) 0xD0;
      return;
    }

    conStr[kk++] = ' ';
    conStr[kk++] = ' ';

    return;
  }

  // **********************************************************
  private int byteToUnsignedInt(byte bytes) {
    return 0x00 << 24 | bytes & 0xff;
  }

}
