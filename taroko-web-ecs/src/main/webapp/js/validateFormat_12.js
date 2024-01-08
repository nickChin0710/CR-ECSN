//19-1227:  JH  ++noallow_cc
//20-1230:  Justin remove % from special char
//21-0104:  Justin remove union from special char
var allow_spec_char=false;
function trimSpaces(s_val) {
  var s_rc = "";
  //--trim Left spaces--
  for (var i = 0; i < s_val.length; i++) {
    if (s_val.charAt(i) != " ") {
      s_rc = s_val.substring(i, s_val.length);
      break;
    }
  }
  //--trim Right spaces--
  for (var i = s_rc.length - 1; i >= 0; i--) {
    if (s_rc.charAt(i) != " ") {
      s_rc = s_rc.substring(0, i + 1);
      break;
    }
  }
  return s_rc;
}
function isNumber(str1) {
  var str = trimSpaces(str1);
  if (str.length == 0) {
    return true;
  }
  var formatexp = /^[0-9]+$/;
  if (!formatexp.exec(str)) {
    return false;
  }
  return true;
}

function isDigitNum(s1) {
  var ss = trimSpaces(s1);
  if (ss.length == 0) {
    return true;
  }
  var formatexp = /^[.0-9]+$/;
  if (!formatexp.exec(ss)) {
    return false;
  }
  return true;
}

function minusNum(s1) {
  var ss = trimSpaces(s1);
  if (ss.length == 0) {
    return true;
  }
  var formatexp = /^\-[.0-9]+$/;
  if (!formatexp.exec(ss)) {
    return false;
  }
  return true;
}

function alpha(s1) {
  var ss = trimSpaces(s1);
  if (ss.length == 0) {
    return true;
  }
  var formatexp = /^[a-zA-Z_^\- ]+$/;
  if (!formatexp.exec(ss)) {
    return false;
  }
  return true;
}

function alphaNum(strValue) {
  if (strValue.length == 0) {
    return true;
  }
  var formatexp = /^[0-9a-zA-Z_^\- ]+$/;
  if (!formatexp.exec(strValue)) {
    return false;
  }
  return true;
}

function upperChar(strValue) {
  if (strValue.length == 0) {
    return true;
  }
  var formatexp = /^[0-9A-Z_^\- ]+$/;
  if (!formatexp.exec(strValue)) {
    return false;
  }
  return true;
}

function lowerChar(strValue) {
  if (strValue.length == 0) {
    return true;
  }
  var formatexp = /^[0-9a-z_^\- ]+$/;
  if (!formatexp.exec(strValue)) {
    return false;
  }
  return true;
}

function isChinese(s1) {
  var ss = trimSpaces(s1);
  if (ss.length == 0) {
    return true;
  }
  if (specialChar(ss)) {
    return false;
  }

  for (var i = 0; i < ss.length; i++) {
    var checkChin = ss.charAt(i);
    if (alphaNum(checkChin)) {
      return false;
    }
  }

  return true;
}

function hasChinese(s1) {
  var ss = trimSpaces(s1);
  var I;

  for (I = 0; I < ss.length; I++) {
    if (escape(ss.charAt(I)).length >= 4) {
      return true;
    }
  }
  return false;
}

function specialChar(strValue) {
  if (strValue.length == 0) { return true; }
  var iChars = "!@#$%^&*()+=-[]\\';,./{}|\":<>?~_";
  for (var i = 0; i < strValue.length; i++) {
    if (iChars.indexOf(strValue.charAt(i)) != -1) {
      return true;
    }
  }
  return false;
}
function noallow_cc(str1) {
  if (str1.trim().length == 0) {
    return false;
  }
  var iChars = "^&\\'\"?~=";
  for (var i = 0; i < iChars.length; i++) {
    if (str1.indexOf(iChars.charAt(i)) >= 0) {
      return true;
    }
  }

// var regStr = /or|union|exec|insert|select|delete|update/i;
  var regStr = /--/i;
  if (str1.match(regStr) != null) {
      return true;
  }

  return false;
}

function dateFormat(strValue) {
  if (strValue.length == 0) {
    return true;
  }
  if (strValue.length != 8 && strValue.length != 10) {
    return false;
  }

  var re_date = /^\s*(\d{1,4})\/(\d{1,2})\/(\d{1,2})\s*$/;
  var re_date2 = /^\s*(\d{1,4})(\d{1,2})(\d{1,2})\s*$/;
  if (!re_date.exec(strValue) && !re_date2.exec(strValue)) {
    return false;
  }

  var n_year = Number(RegExp.$1),
    n_month = Number(RegExp.$2),
    n_day = Number(RegExp.$3);

  if (n_year < 200) {
    n_year += 1911;
  }
  if (n_month < 1 || n_month > 12) {
    return false;
  }
  var d_numdays = new Date(n_year, n_month, 0);
  if (n_day > d_numdays.getDate()) {
    return false;
  }

  return true;
}

function dateYYYYMM(strValue) {
  if (strValue.length == 0) {
    return true;
  }
  if (strValue.length != 6 && strValue.length != 7) {
    return false;
  }

  var re_date = /^\s*(\d{1,4})\/(\d{1,2})\s*$/;
  var re_date2 = /^\s*(\d{1,4})(\d{1,2})\s*$/;
  if (!re_date.exec(strValue) && !re_date2.exec(strValue)) {
    return false;
  }

  var n_year = Number(RegExp.$1),
    n_month = Number(RegExp.$2);

  if (n_month < 1 || n_month > 12) {
    return false;
  }
  return true;
}

function dtval(d, e) {
  if (e.keyCode == 8 || e.keyCode == 9) {
    return;
  }

  var pK = e ? e.which : window.event.keyCode;
  if (pK == 8) {
    d.value = substr(0, d.value.length - 1);
    return;
  }
  var dt = d.value;
  var da = dt.split("/");
  dt = da.join("/");
  if (dt.length == 4 || dt.length == 7) dt += "/";
  d.value = dt;
}

function checkFormat() {
  var types = new Array("input", "select", "textarea");
  var aElems = null, aa_elmTxt=[];
  var setType = "", subAttr = "", setRequ = "", setMesg = "", errFlag = "N", checkFlag = "N", svName = "";
  var ls_data="", ls_msg = "", ls_func="";
  var idx = 0;
  var n = parseInt(levelCode);
  n = n + tabsInd * maxLevel;
  //-清除-
  ls_func =funCode.substring(0,1).toUpperCase();
  if (ls_func == "L") {
    return true;
  }

  for (var i = 0; i < types.length; i++) {
    if (newWindow == "Y") {
      aElems = document.dataForm.getElementsByTagName(types[i]);
    } else {
      aElems = window.dataFrame.frames[n].document.dataForm.getElementsByTagName(types[i]);
    }

    for (var j = 0; j < aElems.length; j++) {
      if (aElems[j].style.background.indexOf("pink") >= 0) {
        aElems[j].removeAttribute("style");
      }
      //-check input.ID-only 不查核-
      if (aElems[j].name == null) { continue; }
      if (aElems[j].type == "button") { continue; }
      if (aElems[j].type == "hidden") { continue; }
      if (aElems[j].type == "text" || aElems[j].type == "textarea") {
        if (aElems[j].disabled || aElems[j].readOnly) { continue; }
      }

      setType = aElems[j].getAttribute("zEdit");
      if (setType == null || setType == "") {
        setType = aElems[j].getAttribute("zeditType");
      }
      setRequ = aElems[j].getAttribute("zRequire");
      setMesg = aElems[j].getAttribute("zMessage");

      if (setType == null) { setType = ""; }
      if (setRequ == null) { setRequ = "N"; }
      if (setMesg == null) { setMesg = ""; }
      subAttr = "";
      //-預設輸入-
      if (aElems[j].type == "text" && setType.length == 0) {
        setType = "any,upper"; //"alphanum,upper";
      }
      setType = setType.toLocaleLowerCase();
      setRequ = setRequ.toLocaleLowerCase();

      if (aElems[j].type == "text" && aElems[j].value.length > 0) {
        aElems[j].value = aElems[j].value.replace(/^\s+|\s+$/gm, "");
      }

      ls_data = trimSpaces(aElems[j].value);
      if (aElems[j].type == "text" || aElems[j].type == "textarea") {
        if (aElems[j].disabled || aElems[j].readOnly) { continue; }
        aa_elmTxt.push(aElems[j]);  //text.arr
      }

      if ( setRequ == "y" && "AU".indexOf(ls_func) >= 0 &&
        (ls_data.length == 0 || (ls_data == "0" && setType.indexOf("number") >= 0))
      ) {
        errFlag = "Y"; aElems[j].style.background = "pink";
        if (ls_msg.length == 0) { ls_msg = "必輸欄位,請輸入資料"; }
        continue;
      }
      if ( "kc".indexOf(setRequ) >= 0 && "QR".indexOf(ls_func) >= 0 &&
        (ls_data.length == 0 || (ls_data == "0" && setType == "number"))
      ) {
        errFlag = "Y"; aElems[j].style.background = "pink";
        if (ls_msg.length == 0) { ls_msg = "必輸欄位,請輸入資料"; }
        continue;
      }
      if (aElems[j].type == "text") {
        if (ls_data.length == 0) { continue; }
        if (setType.indexOf(",") != -1) {
          var cvtString = new Array();
          cvtString = setType.split(",");
          setType = cvtString[0].toLowerCase();
          subAttr = cvtString[1].toLowerCase();
        }

        var maxLength = 0;
        if (
          aElems[j].attributes.maxLength &&
          aElems[j].attributes.maxLength.specified
        ) {
          maxLength = aElems[j].attributes.maxLength.nodeValue;
        }
        if (maxLength > 0) {
          if (!checkMaximum(maxLength, ls_data)) {
            errFlag = "Y"; aElems[j].style.background = "pink";
            if (ls_msg.length == 0) { ls_msg = "輸入資料超過長度"; }
          }
        }

        if (setType == "any" && subAttr != "cantw") {
          if (hasChinese(ls_data)) {
            errFlag = "Y"; aElems[j].style.background = "pink";
            if (ls_msg.length == 0) { ls_msg = "不可輸入中文"; }
          }
        }

        if (setType == "alpha" || setType == "alphanum" || setType == "any") {
          if (subAttr == "lower") {
            aElems[j].value = ls_data.toLowerCase();
          } else if (subAttr == "upper") {
            aElems[j].value = ls_data.toUpperCase();
          }
        }
        //-data-type-
        if (setType == "number") {
          if (!isNumber(ls_data)) {
            errFlag = "Y"; aElems[j].style.background = "pink";
            if (ls_msg.length == 0) { ls_msg = "只允許輸入數字"; }
          }
        } else if (setType == "dignumber") {
          if (!isDigitNum(ls_data)) {
            errFlag = "Y"; aElems[j].style.background = "pink";
            if (ls_msg.length == 0) { ls_msg = "只允許輸入含小數點及數字"; }
          }
        } else if (setType == "canminus") {
          if (!minusNum(ls_data) && !isDigitNum(ls_data)) {
            errFlag = "Y"; aElems[j].style.background = "pink";
            if (ls_msg.length == 0) { ls_msg = "只允許輸入含[小數點,負號]之數字"; }
          }
        } else if (setType == "alpha") {
          if (!alpha(ls_data)) {
            errFlag = "Y"; aElems[j].style.background = "pink";
            if (ls_msg.length == 0) { ls_msg = "只允許輸入英文字母"; }
          }
        } else if (setType == "alphanum") {
          if (!alphaNum(ls_data)) {
            errFlag = "Y"; aElems[j].style.background = "pink";
            if (ls_msg.length == 0) { ls_msg = "只允許輸入英文字母及數字"; }
          }
        } else if (setType == "date") {
          if (subAttr == "yyyymmdd" || subAttr == "ymd") {
            if (!dateFormat(ls_data)) {
              errFlag = "Y"; aElems[j].style.background = "pink";
              if (ls_msg.length == 0) { ls_msg = "日期格式錯誤"; }
            }
          } else if (subAttr == "yyyymm" || subAttr == "yymm") {
            if (!dateYYYYMM(ls_data)) {
              errFlag = "Y"; aElems[j].style.background = "pink";
              if (ls_msg.length == 0) { ls_msg = "日期格式錯誤"; }
            }
          }
        }
      } //-text.edit-
      else if (aElems[j].type == "checkbox" || aElems[j].type == "radio") {
        if (aElems[j].name != svName) {
          checkFlag = "N"; idx = j;
        }
        if (aElems[j].checked) {
          checkFlag = "Y";
        } else if (aElems[j].name == svName && checkFlag == "N") {
          if (
            (setRequ == "y" && "AU".indexOf(ls_func) >= 0) ||
            ("QR".indexOf(ls_func) >= 0 && "kc".indexOf(setRequ) >= 0)
          ) {
            errFlag = "Y";
            for (var k = idx; k <= j; k++) {
              aElems[k].style.background = "pink";
            }
            if (setMesg.length > 0) {
              alert("請選擇 :" + setMesg);
            }
          }
        }
        svName = aElems[j].name;
      } //-checkBox-
      else if (aElems[j].type == "select-one") {
        if (setRequ == "Y" && ls_data == "###") {
          errFlag = "Y";
          aElems[j].style.background = "pink";
          if (setMesg.length > 0) {
            errFlag = "Y";
            alert("請選擇  :" + setMesg);
          }
        }
      } //-select-one-
      else if (aElems[j].type == "select-multiple") {
        var selFlag = "N";
        for (k = 0; k < aElems[j].length; k++) {
          if (aElems[j].options[k].selected) {
            selFlag = "Y";
          }
        }
        if (setRequ == "Y" && selFlag == "N") {
          aElems[j].style.background = "pink";
          if (setMesg.length > 0) {
            errFlag = "Y";
            alert("請選擇  :" + setMesg);
          }
        }
      } //-select-multi-
    } // end of for loop TagName
  } // end of for loop types

  if (errFlag == "Y") {
    if (ls_msg.length > 0) { alert(ls_msg); }
    return false;
  }
  //-特殊符號-
  // 20201218 filter all func
  // if (",A,U,C".indexOf(ls_func) >0 && allow_spec_char==false) {
  if (allow_spec_char==false) {
    for (var j = 0; j < aa_elmTxt.length; j++) {
      ls_data =trimSpaces(aa_elmTxt[j].value);
      if (ls_data.length ==0) { continue; }
      if (noallow_cc(ls_data)) {
        errFlag = "Y"; aa_elmTxt[j].style.background = "pink";
      }
    }
    if (errFlag=="Y") {
      alert("不可輸入特殊符號或特殊字串");
      return false;
    }
  }
  allow_spec_char =false;
  return true;
} // end of checkFormat

function checkMaximum(maxlen, checkText) {
  if (checkText.length > maxlen) {
    return false;
  }
  return true;
}

function formatDate(s_ymd) {
  var ss = s_ymd.trim();
  if (ss.length <= 4) {
    return s_ymd;
  }
  if (ss.length <= 6) {
    return ss.substr(0, 4) + "/" + ss.substr(4, 2);
  }
  return ss.substr(0, 4) + "/" + ss.substr(4, 2) + "/" + ss.substr(6, 2);
}

function convertToFullWidth(str){
  var result = "";
  var len = str.length;
  for(var i=0;i<len;i++){
      var code = str.charCodeAt(i);
      if (code>=0x0021 && code<=0x007E) {
        //全形與半形相差（除空格外）：65248(十進位制)
        code += 65248;
      }else if(code==0x0020){
        //處理空格
        code = 0x03000; 
      }
      result += String.fromCharCode(code);
  }
  return result;
}

function convertInputToFullWidth(element){
  element.value = convertToFullWidth(element.value);
}
