/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
* 109-07-06   V1.00.00  shiyuqi       updated for project coding standard     *
******************************************************************************/
package hdata;

public class RskTrialDataJcic extends BaseBin {

  public String batchNO = "";
  public String idNo = "";
  public String idPSeqno = "";
  public String jcicRespDate = "";
  public String jcicNo = "";
  public String dataFlag = "N";
  public double tolScore = 0;
  public double jcic00101 = 0;
  public double jcic00102 = 0;
  public double jcic00103 = 0;
  public double jcic00104 = 0;
  public double jcic00201 = 0;
  public double jcic00202 = 0;
  public double jcic00203 = 0;
  public double jcic00204 = 0;
  public double jcic00301 = 0;
  public double jcic00302 = 0;
  public double jcic00303 = 0;
  public double jcic00401 = 0;
  public double jcic00402 = 0;
  public double jcic00501 = 0;
  public double jcic00502 = 0;
  public double jcic00503 = 0;
  public double jcic00504 = 0;
  public double jcic00601 = 0;
  public double jcic00602 = 0;
  public double jcic00701 = 0;
  public double jcic00702 = 0;
  public double jcic00801 = 0;
  public double jcic00802 = 0;
  public double jcic00803 = 0;
  public double jcic009 = 0;
  public int jcic01001 = 0;
  public int jcic01002 = -1;
  public int jcic011 = 0;
  public String jcic01201 = "N";
  public int jcic01202 = 0;
  public int jcic01203 = -1;
  public int jcic01204 = -1;
  public int jcic01205 = -1;
  public String jcic013 = "N";
  public String jcic01401 = "N";
  public int jcic01402 = 0;
  public int jcic01403 = 0;
  public String jcic015 = "N";
  public String jcic016 = "N";
  public double jcic01701 = 0;
  public double jcic01702 = 0;
  public double jcic01801 = 0;
  public double jcic01802 = 0;
  public String jcic019 = "N";
  public String jcic01901 = "N";
  public double jcic02001 = 0;
  public double jcic02002 = 0;
  public double jcic021 = 0;
  public double jcic02201 = 0;
  public double jcic02202 = 0;
  public int jcic02301 = -1;
  public int jcic02302 = -1;
  public double jcic024 = 0;
  public String jcic025 = "N";
  public double jcic026 = 0;
  public double jcic027 = 0;
  public double jcic028 = 0;
  public double jcic029 = 0;
  public String jcic030 = "N";
  public String jcic031 = "N";
  public String jcic032 = "N";
  public int jcic033 = 0;
  public String jcic034 = "N";
  public int jcic035 = 0;
  public String jcic036 = "A";
  public String jcic037 = "N";
  public double jcic038 = -1;
  public double jcic039 = 0;
  public double jcic040 = 0;
  public String bankLimit = "";
  public int jcic02303 = 0;
  public int jcic02501 = 0;
  public String jcic03001 = "";
  public String jcic03002 = "";
  public String jcic03101 = "";
  public String jcic03102 = "";
  public String jcic041 = "";
  public int jcic01301 = 0;
  public double jcic00205 = 0;


  @Override
  public void initData() {
    batchNO = "";
    idNo = "";
    idPSeqno = "";
    jcicRespDate = "";
    jcicNo = "";
    dataFlag = "N";
    tolScore = 0;
    jcic00101 = 0;
    jcic00102 = 0;
    jcic00103 = 0;
    jcic00104 = 0;
    jcic00201 = 0;
    jcic00202 = 0;
    jcic00203 = 0;
    jcic00204 = 0;
    jcic00205 = 0;
    jcic00301 = 0;
    jcic00302 = 0;
    jcic00303 = 0;
    jcic00401 = 0;
    jcic00402 = 0;
    jcic00501 = 0;
    jcic00502 = 0;
    jcic00503 = 0;
    jcic00504 = 0;
    jcic00601 = 0;
    jcic00602 = 0;
    jcic00701 = 0;
    jcic00702 = 0;
    jcic00801 = 0;
    jcic00802 = 0;
    jcic00803 = 0;
    jcic009 = 0;
    jcic01001 = 0;
    jcic01002 = 0;
    jcic011 = 0;
    jcic01201 = "N";
    jcic01202 = -1;
    jcic01203 = -1;
    jcic01204 = -1;
    jcic01205 = -1;
    jcic013 = "N";
    jcic01301 = 0;
    jcic01401 = "N";
    jcic01402 = 0;
    jcic01403 = 0;
    jcic015 = "N";
    jcic016 = "N";
    jcic01701 = 0;
    jcic01702 = 0;
    jcic01801 = 0;
    jcic01802 = 0;
    jcic019 = "N";
    jcic01901 = "N";
    jcic02001 = 0;
    jcic02002 = 0;
    jcic021 = 0;
    jcic02201 = 0;
    jcic02202 = 0;
    jcic02301 = -1;
    jcic02302 = -1;
    jcic024 = 0;
    jcic025 = "N";
    jcic026 = 0;
    jcic027 = 0;
    jcic028 = 0;
    jcic029 = 0;
    jcic030 = "N";
    jcic031 = "N";
    jcic032 = "N";
    jcic033 = 0;
    jcic034 = "N";
    jcic035 = 0;
    jcic036 = "A";
    jcic037 = "N";
    jcic038 = -1;
    jcic039 = 0;
    jcic040 = 0;
    bankLimit = "";
    jcic02303 = 0;
    jcic02501 = 0;
    jcic03001 = "";
    jcic03002 = "";
    jcic03101 = "";
    jcic03102 = "";
    jcic041 = "";

    rowid = "";
  }

}
