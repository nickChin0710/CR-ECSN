/**
 * 2019-1105    JH      initial
 *  */

var jsonLen = new Object();
var jsonVal = new Object();
var jsonAddName = new Array();
var jsonAddValue = new Array();
var jsonRespMesg = new Array();
var initInd = 0;
var resetFalg = "";
var disableFlag = "";
var autoJSON = false;

function processAJAX() {
  top.disableEffect();
  top.actionCode = "AJAX";
  top.methodName = "actionFunction";

  var httpRequest = false;

  if (window.XMLHttpRequest) { // code for IE7+, Firefox, Chrome, Opera, Safari
    httpRequest = new XMLHttpRequest();
  } else
    if (window.ActiveXObject) { // code for IE6, IE5
      try {
        httpRequest = new ActiveXObject("Msxml2.XMLHTTP");
      } catch (ex) {
        httpRequest = new ActiveXObject("Microsoft.XMLHTTP");
      }
    }

  if (!httpRequest) {
    alert('Giving up :( Cannot create an XMLHTTP instance');
    return false;
  }

  var servurl = "MainControl?AJAX_JASON=" + createJSON();
  httpRequest.onreadystatechange = function () {
    serverResponse(httpRequest);
  };
  httpRequest.open('POST', encodeURI(servurl), true);
  httpRequest.send(null);
}

function serverResponse(httpRequest) {
  var respText = "";
  if (httpRequest.readyState == 4 && httpRequest.status == 200) {
    respText = httpRequest.responseText;
    if (respText.length < 10) {
      top.respMesg = "處理錯誤";
      top.dispMessage();
    }
    decodeJSON(respText);
    ajaxResponse();
  }
}

function resetJSON() {
  jsonLen = new Object();
  jsonVal = new Object();
  jsonAddName = new Array();
  jsonAddValue = new Array();
  initInd = 0;
  top.respMesg = "";
  resetFalg = "";
  disableFlag = "";
  return;
} // resetJSON

function addJSON(initName, initValue) {
  jsonAddName[initInd] = initName;
  jsonAddValue[initInd] = initValue;
  initInd++;

  return;
} // addJSON

function createJSON() {
  var types = new Array("select", "input", "textarea", "label");
  var aElements = null;
  var jcoma = "", jsonString = "", checkData = "";

  top.createHideData();
  document.dataForm.HIDE.value = top.hideData;

  if (autoJSON) {
    for (var i = 0; i < types.length; i++) {
      aElements = document.dataForm.getElementsByTagName(types[i]);
      for (var j = 0; j < aElements.length; j++) {
        if (aElements[j].type == "checkbox" || aElements[j].type == "radio") {
          if (aElements[j].checked == false) {
            continue;
          }
        }

        var comName = "", comValue = "";
        if (types[i] == "label") {
          comName = aElements[j].id;
          comValue = aElements[j].innerHTML;
        } else {
          comName = aElements[j].name;
          comValue = aElements[j].value;
        }

        comValue = comValue.replace(/\"/g, "@~");
        comValue = comValue.replace(/%/g, "~;");
        comValue = comValue.replace(/&/g, ";-");
        comValue = comValue.replace(/#/g, "@=");

        var n = checkData.search(('@' + comName));
        if (n == -1) {
          jsonString = jsonString + jcoma + '"' + comName + '":"' + comValue + '"';
        } else {
          jsonString = jsonString + '},{"' + comName + '":"' + comValue + '"';
          checkData = "";
        }
        checkData = checkData + '@' + comName;
        jcoma = ",";
      }
    }
  }

  if (!autoJSON) {
    addJSON("HIDE", top.hideData);
  }

  for (var i = 0; i < jsonAddName.length; i++) {
    var n = checkData.search(jsonAddName[i]);
    var cvtValue = jsonAddValue[i].replace(/\"/g, "@~");
    cvtValue = cvtValue.replace(/%/g, "~;");
    cvtValue = cvtValue.replace(/&/g, ";-");
    cvtValue = cvtValue.replace(/#/g, "@=");

    if (n == -1) {
      jsonString = jsonString + jcoma + '"' + jsonAddName[i] + '":"' + cvtValue + '"';
    } else {
      jsonString = jsonString + "},{'" + jsonAddName[i] + "':'" + cvtValue + "'";
      checkData = "";
    }
    checkData = checkData + '@' + jsonAddName[i];
    jcoma = ",";
  }

  jsonString = '{"ajaxInfo":[{' + jsonString + '}]}';
  return jsonString;
} // createJSON

function decodeJSON(jsonData) {
  var obj = eval("(function(){return " + jsonData + ";})()");

  for (var outKey in obj) {
    for (var i = 0; i < obj[outKey].length; i++) {
      for (var inKey in obj[outKey][i]) {
        restoreField(inKey, obj[outKey][i][inKey]);
      }
    }
  }

  if (top.respCode == "99") {
    alert(top.respMesg);
  } else
    if (top.respCode != "00") {
      jsonRespMesg = top.respMesg.split("**");
      top.respMesg = jsonRespMesg[0];
      if (jsonRespMesg.length > 1) {
        alert(jsonRespMesg[1]);
      }
    }

  if (top.errField.length > 2) {
    top.ajaxFlag = "Y";
    top.respControl();
  }

  if (resetFalg == "Y") {
    resetField();
  }

  top.dispMessage();

  resetFalg = "";
  disableFlag = "";
}

function restoreField(fieldName, fieldValue) {
  fieldValue = fieldValue.replace(/@~/g, "\"");
  fieldValue = fieldValue.replace(/~;/g, "%");
  fieldValue = fieldValue.replace(/;-/g, "&");
  fieldValue = fieldValue.replace(/@=/g, "#");
  saveJSON(fieldName, fieldValue);

  if (fieldName == "RC_CODE" && fieldValue == "00") {
    if (top.actionCode == "A" || top.actionCode == "D") {
      resetFalg = "Y";
    }

    if (top.actionCode == "Q" || top.actionCode == "D") {
      disableFlag = "Y";
    }
  }

  if (fieldName == "RC_CODE") {
    top.respCode = fieldValue;
  }

  if (fieldName == "RC_MESG") {
    top.respMesg = fieldValue;
  }

  if (fieldName == "ERR_FIELD") {
    top.errField = fieldValue;
    return;
  }

  if (fieldName.length > 2 && fieldName.substring(0, 2) == "@_") {
    document.getElementById(fieldName.substring(2)).innerHTML = fieldValue;
    return;
  }

  //-JJJ:>>>-
  var aElements = null;
  aElements = document.getElementsByName(fieldName);
  if (aElements.length == 0) {
    var aObj = document.getElementById(fieldName);
    if (aObj == null) {
      return;
    }

    if (typeof aObj.type == 'undefined') {
      aObj.innerHTML = fieldValue;
      //alert("-->"+fieldName+", type="+aObj.type); //+aObj.type);
    }
    return;
  }
  //-JJJ:<<<-

  for (var j = 0; j < aElements.length; j++) {
    if (aElements[j].type == "checkbox" || aElements[j].type == "radio") {
      if (aElements[j].value == fieldValue && fieldValue.length > 0) {
        aElements[j].checked = true;
      } else {
        aElements[j].checked = false;
      }
    } else
      if (aElements[j].type == "select-one" || aElements[j].type == "select-multiple") {
        for (k = 0; k < aElements[j].length; k++) {
          if (aElements[j].options[k].value == fieldValue && fieldValue.length > 0) {
            aElements[j].options[k].selected = true;
          }
        }
      } else
        if (typeof aElements[j].type == "undefined") {
          aElements[j].innerHTML = fieldValue;
        } else {
          aElements[j].value = fieldValue;
        }
  }
}

function saveJSON(fieldName, fieldValue) {
  var ck = 0;
  var ck = jsonLen[fieldName];
  if (ck == null) {
    ck = 0;
  }
  jsonLen[fieldName] = ck + 1;
  jsonVal[fieldName + ck] = fieldValue;
  return;
}

function getJSONlength(fieldName) {
  var n = 0;
  n = jsonLen[fieldName];
  return n;
}

function getJSONvalue(fieldName, ind) {
  var val = "";
  try {
    val = jsonVal[fieldName + ind];
    if (val.length == 0) { val = ""; }
  } catch (ex) {
    val = "";
  }
  return val;
}
function getJson(fieldName) {
  return getJSONvalue(fieldName,0);
}

function resetField() {
  var types = new Array("select", "input", "textarea", "label");

  var aElements = null;

  for (var i = 0; i < types.length; i++) {
    aElements = document.dataForm.getElementsByTagName(types[i]);
    for (var j = 0; j < aElements.length; j++) {
      if (aElements[j].type == "button") {
        continue;
      }
      if (aElements[j].type == "hidden" && aElements[j].name == "HIDE") {
        continue;
      }

      if (types[i] == "label") {
        aElements[j].innerHTML = "";
        continue;
      }

      if (aElements[j].type == "checkbox" || aElements[j].type == "radio" && j > 0) {
        aElements[j].checked = false;
      } else
        if (aElements[j].type == "select-one" || aElements[j].type == "select-multiple") {
          for (k = 0; k < aElements[j].length; k++) {
            if (k > 0) {
              aElements[j].options[k].selected = false;
            }
          }
        } else {
          aElements[j].value = "";
        }
    }
  }
  return;
} // resetField
