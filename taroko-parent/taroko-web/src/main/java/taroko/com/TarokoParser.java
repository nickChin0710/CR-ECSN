/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
*  109-04-21  V1.00.01  Zuwei       code format                              *
*  109-08-14  V1.00.01  Zuwei      fix code scan issue verify sql、path、輸出瀏覽器咨詢      *
*  109-09-04  V1.00.01  yanghan     解决Portability Flaw: Locale Dependent Comparison问题    * 
*  109-10-14  V1.00.01  Zuwei      修正loginUser爲空時水印圖片path包含伺服器路徑問題      *
*  109-10-14  V1.00.01  Zuwei       字串”系統日志”改為”系統日誌”      *
*  110-01-05  V1.00.10  JustinWu   updated for XSS
*  110-01-08  V1.00.11   shiyuqi       修改无意义命名
*  111-01-17  V1.00.12  Justin       logger -> getNormalLogger()
*  111-01-19  V1.00.13  Justin     fix Missing Check against Null            *
******************************************************************************/
package taroko.com;
/*UI: HTML公用程式
 * 2019-0328:  inputName[]
 * 2017-1206: 多筆radio
 * 20171130: Plugin checkbox, radio format
 * */
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import Dxc.Util.SecurityUtil;
import taroko.base.BaseData;

@SuppressWarnings({"unchecked", "deprecation"})
public class TarokoParser {
  TarokoCommon wp = null;

  public void parseNameType(TarokoCommon wr) {
    this.wp = wr;
    InputStreamReader fr = null;
    BufferedReader br = null;

    try {
      String htmlInput = "", inputStr = "", cvtStr = "", typeStr = "";
      String scanName = "", scanValue = "", scanType = "", fieldName = "", comName = "";
      int recLength = 0, scanLength = 0, pnt = 0;
      int checkInputU = 0, checkSelectU = 0, checkInput = 0, checkSelect = 0, checkTextArea = 0;

      wp.packageDir = wp.packageName.replaceAll("\\.", "/") + "/";

      if (wp.respHtml.length() >= 6 && wp.respHtml.substring(0, 6).equals("Taroko")) {
        wp.packageDir = "taroko/com/";
      }

      if (wp.pluginHtml) {
        htmlInput = TarokoParm.getInstance().getHtmlDir() + wp.pluginFile;
      } else {
        htmlInput = TarokoParm.getInstance().getHtmlDir() + wp.packageDir + wp.respHtml + ".html";
      }

      if (wp.jsonCode.equals("Y") || wp.menuCode.equals("Y")) {
        return;
      }

      scanName = "NAME=\"";
      scanType = "TYPE=\"";
      scanValue = "VALUE=\"";
      scanLength = scanName.length();

      // filter escape char
      while (htmlInput.indexOf("..") >= 0) {
          htmlInput = htmlInput.replace("..", "");
      }
      // verify path
      String tempPath = SecurityUtil.verifyPath(htmlInput);
      htmlInput = tempPath;
      fr = new InputStreamReader(new FileInputStream(htmlInput), "UTF-8");
      br = new BufferedReader(fr);

      // wp.ind = 0;
      LinkedList<String> aacol = new LinkedList<>();
      while (br.ready()) {
        inputStr = br.readLine();
        if (inputStr == null) inputStr = "";
        inputStr = inputStr.toUpperCase();
        while (true) {
          recLength = inputStr.length();

          checkInput = inputStr.indexOf("<INPUT");
          checkSelect = inputStr.indexOf("<SELECT");
          checkTextArea = inputStr.indexOf("<TEXTAREA");
          if (checkInput == -1 && checkSelect == -1 && checkTextArea == -1) {
            break;
          }

          pnt = inputStr.indexOf(scanName);
          if (pnt == -1) {
            break;
          }
          cvtStr = inputStr.substring(pnt + scanLength, recLength);
          pnt = cvtStr.indexOf("\"");
          if (pnt == -1) {
            break;
          }

          fieldName = cvtStr.substring(0, pnt).trim().toUpperCase(Locale.TAIWAN);
          if (!fieldName.equals(comName)) {
            if (fieldName.length() > 2) {
              if (fieldName.substring(0, 2).equals("${")) {
                fieldName = fieldName.substring(2, fieldName.length() - 1);
              }
            }

            aacol.add(fieldName);
            // wp.inputName[wp.ind] = fieldName;
            // wp.ind++;
            if (checkSelect != -1) {
              wp.putType(fieldName, "SELECT");
            } else if (checkTextArea != -1) {
              wp.putType(fieldName, "TEXTAREA");
            } else if (checkInput != -1) {
              pnt = inputStr.indexOf(scanType);
              if (pnt >= 0) {
                typeStr = inputStr.substring(pnt + scanLength, recLength);
                pnt = typeStr.indexOf("\"");
                if (pnt >= 0) {
                  wp.putType(fieldName, typeStr.substring(0, pnt));
                }
              }
            }
          }

          comName = fieldName;
          inputStr = cvtStr.substring(pnt + 1, cvtStr.length());
          recLength = inputStr.length();
          if (inputStr.length() < scanName.length()) {
            break;
          }
        } // end of while 1

      } // end of while 2
      wp.inputName = new String[aacol.size()];
      aacol.toArray(wp.inputName);
    } // end of try

    catch (Exception ex) {
      wp.expMethod = "parseNameType";
      wp.expHandle(ex);
      return;
    }

    finally {
      try {
        if (fr != null) {
          fr.close();
        }
        fr = null;
        if (br != null) {
          br.close();
        }
        br = null;
      } catch (Exception ex2) {
      }
    }
  } // End of parseNameType

  public void parseOutput(TarokoCommon wr) {
    this.wp = wr;
    // FileReader fr = null;
    InputStreamReader fr = null;
    BufferedReader br = null;
    // -浮水印-
    if (wp.loginUser.length() > 0) {
      // -linux-
      wp.colSet("WATER_MARK", "WebData/work/" + wp.loginUser + ".png?version=" + wp.sysTime);
      // -window-
      // wp.col_set("WATER_MARK",wp.dataRoot+ "/work/"+wp.loginUser+".png?version="+wp.sysTime);
    } else {
      wp.colSet("WATER_MARK", "WebData/work/0000.png?version=" + wp.sysTime);
//      wp.colSet("WATER_MARK", wp.dataRoot + "/work/" + "0000.png?version=" + wp.sysTime);
    }

    try {
      String htmlOuput = "", fieldName = "", menuControl = "";
      String inputStr = "", cvtStr = "", scanStr = "", replaceStr = "", frontStr = "", typeName =
          "", cvtName = "";
      String dynamicStat = "", dynamicBegin = "", dynamicEnd = "", skip = "", initValue = "";
      int recLength = 0, scanLength = 0, pnt = 0;
      int dynamicCnt = 0, qi = 0, str = 0, cnt = 0;
      String colName = "";

      if (wp.menuCode.equals("Y")) {
        outBuffer(wp.ajaxInfo);
        return;
      } else if (wp.jsonCode.equals("Y")) {
        outBuffer((wp.responseJson + wp.newLine));
        return;
      }

      if (wp.pluginHtml) {
        htmlOuput = TarokoParm.getInstance().getHtmlDir() + wp.pluginFile;
      } else {
        htmlOuput = TarokoParm.getInstance().getHtmlDir() + wp.packageDir + wp.respHtml + ".html";
      }

      // verify path
      String tempPath = SecurityUtil.verifyPath(htmlOuput);
      htmlOuput = tempPath;
      fr = new InputStreamReader(new FileInputStream(htmlOuput), "UTF-8");
      br = new BufferedReader(fr);

      ArrayList<String> arrList = new ArrayList<String>();

      scanStr = "${";
      scanLength = scanStr.length();
      skip = "";

      while (br.ready()) {
        if (dynamicStat.equals("Y")) {
          inputStr = (String) arrList.get(cnt);
          cnt++;
        } else {
          inputStr = br.readLine();
          if (inputStr == null) inputStr = "";
        }
        
        if (!TarokoParm.getInstance().getJsVersion().equals("1.0") && inputStr.indexOf("<script") != -1
            && inputStr.indexOf(".js") != -1) {
          String jsVer = "js?ver=" + TarokoParm.getInstance().getJsVersion() + "\"";
          inputStr = inputStr.replaceFirst("js\"", jsVer);
        } // JS 版本控制

        if (!TarokoParm.getInstance().getCssVersion().equals("1.0") && inputStr.indexOf("stylesheet") != -1
            && inputStr.indexOf(".css") != -1) {
          String cssVer = "css?ver=" + TarokoParm.getInstance().getCssVersion() + "\"";
          inputStr = inputStr.replaceFirst("css\"", cssVer);
        } // CSS 版本控制

        recLength = inputStr.length();

        if (inputStr.indexOf("BEGIN DYNAMIC") != -1) {
          dynamicBegin = "Y";
          continue;
        }

        if (inputStr.indexOf("END DYNAMIC") != -1) {
          dynamicEnd = "Y";
        }

        if (dynamicBegin.equals("Y") && !dynamicEnd.equals("Y")) {
          arrList.add(inputStr);
          cnt++;
        }

        if (dynamicBegin.equals("Y") && dynamicEnd.equals("Y")) {
          dynamicStat = "Y";
          dynamicBegin = "N";
          dynamicEnd = "N";
          dynamicCnt = cnt;
          cnt = 0;
          continue;
        }

        if (dynamicBegin.equals("Y")) {
          continue;
        }

        while (true) {
          pnt = inputStr.indexOf(scanStr);
          if (pnt == -1) {
            break;
          }
          frontStr = inputStr.substring(0, pnt);
          cvtStr = inputStr.substring(pnt + scanLength, recLength);
          pnt = cvtStr.indexOf("}");
          if (pnt == -1) {
            break;
          }

          wp.orgField = cvtStr.substring(0, pnt);
          fieldName = cvtStr.substring(0, pnt).toUpperCase();

          initValue = "";
          String[] checkName = fieldName.split(",");
          if (checkName.length == 2) {
            fieldName = checkName[0];
            initValue = checkName[1];
          }

          cvtStr = cvtStr.substring(pnt + 1, cvtStr.length());
          cvtName = fieldName;
          if (fieldName.length() >= 2 && fieldName.substring(0, 2).equals("#_")) {
            // -轉換{#_xxx-99}-
            cvtName = fieldName.substring(2, fieldName.length());
            wp.setValue(fieldName, (cvtName + str).toLowerCase(), str);
          }
          int pos = cvtName.lastIndexOf("-");
          if (pos > 0) {
            colName = cvtName.substring(0, pos);
          } else
            colName = cvtName;

          typeName = wp.getType(fieldName);
          if (typeName.length() == 0) {
            typeName = wp.getType(colName);
          }
          if (typeName.equals("CHECKBOX")) {
            wp.setValue(colName + "-" + wp.getValue(colName, str), "checked", str);
          } else if (typeName.equals("RADIO")) {
            wp.setValue(colName + "-" + wp.getValue(colName, str), "checked", str);
          } else if (typeName.equals("SELECT")) {
            wp.setValue(colName + "-" + wp.getValue(colName, str), "selected", str);
          }

          fieldName = wp.convertField(fieldName);
          wp.userTagRr = 0;

          if (dynamicStat.equals("Y")) {
            replaceStr = wp.getValue(fieldName, str);
            wp.userTagRr = str;
          } else {
            replaceStr = wp.getValue(fieldName, 0);
          }

          if (fieldName.equals("MENU_SCRIPT")) {
            menuControl = "Y";
            replaceStr = "";
          }

          if (wp.descField.equals("PLUGIN")) { // PLUGIN 為 同 一網頁為其他網頁共用
            wp.htmlBuf = new StringBuffer();
            String[] cvtTmp = wp.orgField.split(":");
            wp.pluginFile = cvtTmp[1].replaceAll("\\.", "/") + ".html";
            wp.pluginHtml = true;
            TarokoParser pr2 = new TarokoParser();
            pr2.parseNameType(wp);
            pr2.parseOutput(wp);
            wp.pluginHtml = false;
            replaceStr = wp.htmlBuf.toString();
            wp.htmlBuf = null;
            pr2 = null;
            wp.descField = "";
          } else {
            replaceStr = wp.convertFormat(fieldName, replaceStr);
          }

          if (wp.initFlag.equals("Y") && initValue.length() > 0) {
            replaceStr = initValue;
          }

          inputStr = frontStr + replaceStr + cvtStr;

          recLength = inputStr.length();
          if (cvtStr.length() < scanStr.length()) {
            break;
          }
        } // end of while 1

        if (menuControl.equals("Y")) {
          outBuffer(wp.menuBuf.toString());
          wp.menuBuf = null;
        } else {
          // outBuffer((inputStr+wp.newLine));
          outBuffer((inputStr));
        }
        menuControl = "";

        if (dynamicStat.equals("Y") && cnt >= dynamicCnt) {
          str++;
          cnt = 0;
        }
        if (str >= (wp.listCount[qi]) && dynamicStat.equals("Y")) {
          qi++;
          str = 0;
          cnt = 0;
          dynamicStat = "N";
          dynamicCnt = 0;
          arrList.clear();
        }

      } // end of while 2

      if (wp.alertMesg.length() > 0 && !wp.pluginHtml) {
        outBuffer((wp.alertMesg + wp.newLine));
      }

      if (!wp.pluginHtml) {
        wp.out.flush();
        wp.out.close();
        wp.out = null;
      }
      arrList.clear();
      arrList = null;
    }

    catch (Exception ex) {
      wp.expMethod = "parseOutput";
      wp.expHandle(ex);
      BaseData.getNormalLogger().error("ERR:000002 - PARSE OUTPUT FAILED : " + wp.dispMesg, ex);
      wp.out.println("ERR:000002 - PARSE OUTPUT FAILED : 系統異常，請查詢系統日誌");
      return;
    }

    finally {
      try {
        if (fr != null) {
          fr.close();
        }
        fr = null;
        if (br != null) {
          br.close();
        }
        br = null;
      } catch (Exception ex2) {
      }
    }
  } // End of parseOutput

  public void outBuffer(String outData) throws Exception {
    if (wp.pluginHtml) {
      wp.htmlBuf.append(outData);
    } else {
      wp.out.println(outData);
      if (wp.menuCode.equals("Y") || wp.jsonCode.equals("Y")) {
        wp.out.flush();
        wp.out.close();
        wp.out = null;
      }
    }
  }

} // End of class TarokoParser
