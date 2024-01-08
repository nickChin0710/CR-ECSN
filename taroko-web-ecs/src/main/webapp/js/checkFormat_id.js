function err_alert(aElem, ss) {
  aElem.style.background = "pink";
  alert(ss);
  return false;
}

function checkFormat_id(obj) {
  var aElem = null;
  var setType = "", subAttr = "", setRequ = "", setMesg = "", errFlag = "N", checkFlag = "N", svName = "";
  var startData = "", endData = "", alertMesg = "";
  var idx = 0;
  aElem = document.getElementById(obj);

  aElem.removeAttribute("style");
  if (aElem.style.background.indexOf("pink")>=0) {
    aElem.removeAttribute("style");
  }
  setType = aElem.getAttribute("zEdit");
  if (setType == null || setType == "") {
    setType = aElem.getAttribute("zeditType");
  }
  setRequ = aElem.getAttribute("zRequire");
  setMesg = aElem.getAttribute("zMessage");

  if (setType == null) { setType = ""; }
  if (setRequ == null) { setRequ = "N"; }
  if (setMesg == null) { setMesg = ""; }
  subAttr = "";

  //-預設輸入-
  if (aElem.type == "text" && setType.length == 0) {
    setType = "any,upper";
  }
  setType = setType.toLowerCase();
  if (aElem.type == "text" && aElem.value.length > 0) {
    aElem.value = aElem.value.replace(/^\s+|\s+$/gm, '');
  }
  setRequ = setRequ.toLocaleLowerCase();
  var value_1 = top.trimSpaces(aElem.value);

  if ((setRequ == "yy") &&
    (value_1.length == 0 || (value_1 == "0" && setType.indexOf("number") >= 0))) {
    return err_alert(aElem, "必輸欄位, 請輸入資料");
  }
  var lb_edit = true;
  if (aElem.type == "text") {
    if (aElem.disabled || aElem.readOnly) {
      lb_edit = false;
    }
  }

  if (aElem.type == "text" && aElem.value.length > 0 && lb_edit) {
    if (setType.indexOf(",") != -1) {
      var cvtString = new Array();
      cvtString = setType.split(",");
      setType = cvtString[0].toLowerCase();
      subAttr = cvtString[1].toLowerCase();
    }

    //-maxLength-
    var maxLength = 0;
    if (aElem.attributes.maxLength && aElem.attributes.maxLength.specified) { maxLength = aElem.attributes.maxLength.nodeValue; }
    if (maxLength > 0 && aElem.disabled == false) {
      if (!top.checkMaximum(maxLength, aElem.value)) { return err_alert(aElem, "輸入資料超過長度"); }
    }
    //-canTW-
    if (setType != "cantw" && subAttr != "cantw") {
      if (top.hasChinese(aElem.value)) { return err_alert(aElem, "不可輸入中文"); }
    }
    else {
      return true;
    }

    //-input-case-
    if (setType == "alpha" || setType == "alphanum" || setType == "any") {
      if (subAttr == "lower") { aElem.value = aElem.value.toLowerCase(); }
      else if (subAttr == "upper") { aElem.value = aElem.value.toUpperCase(); }
    }
    //-number-format-
    if (setType == "number") {
      if (!top.isNumber(aElem.value)) { return err_alert(aElem, "只允許輸入數字"); }
      return true;
    }
    else if (setType == "dignumber") {
      if (!top.isDigitNum(aElem.value)) { return err_alert(aElem, "只允許輸入含小數點及數字"); }
      return true;
    }
    else if (setType == "canminus") {
      if (!top.minusNum(aElem.value) && !top.isDigitNum(aElem.value)) { return err_alert(aElem, "只允許輸入含小數點之數字(可為負值)"); }
      return true;
    }
    else if (setType == "alpha") {
      if (!top.alpha(aElem.value)) { return err_alert(aElem, "只允許輸入英文字母"); }
      return true;
    }
    else if (setType == "alphanum") {
      if (!top.alphaNum(aElem.value)) { return err_alert(aElem, "只允許輸入英文字母及數字"); }
      return true;
    }
    else if (setType == "date") {
      if (subAttr == "yyyymmdd" || subAttr == "ymd") {
        if (!top.dateFormat(aElem.value)) { return err_alert(aElem, "日期格式錯誤"); }
      }
      else
        if (subAttr == "yyyymm" || subAttr == "yymm") {
          if (!top.dateYYYYMM(aElem.value)) { return err_alert(aElem, "日期格式錯誤"); }
        }
      return true;
    }
    else
      if (aElem.type == "select-one") {
        if ((setRequ == "yy") && aElem.value == "###") { return err_alert(aElem, "請選擇資料"); }
        return true;
      }
      else
        if (aElem.type == "select-multiple") {
          var selFlag = "N";
          for (k = 0; k < aElem.length; k++) {
            if (aElem.options[k].selected) { selFlag = "Y"; }
          }
          if (setRequ == "yy" && selFlag == "N") { return err_alert(aElem, "請選擇資料"); }
          return true;
        }
        else
          if (aElem.type == "textarea") { return true; }

  }  //-input-text-

  return true;
}
