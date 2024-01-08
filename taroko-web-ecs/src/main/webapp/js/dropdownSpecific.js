  var OptIndex = 0;

  var vEditableOptionText_A = "--?--";

  var vUseActualTexbox_A = "no";

  var vPreviousSelectIndex_A = 0;

  var vSelectIndex_A = 0;

  var vSelectChange_A = 'MANUAL_CLICK';

  function fnLeftToRight(getdropdown)
  { getdropdown.style.direction = "ltr"; }

  function fnRightToLeft(getdropdown)
  { getdropdown.style.direction = "rtl"; }


  function FindKeyCode(e)
  {
    if( e.which ) //NetscapeFirefoxChrome
      { keycode=e.which;   }
    else //Internet Explorer
      { keycode=e.keyCode; }
    return keycode;
  }

  function FindKeyChar(e)
  {
    keycode = FindKeyCode(e);
    if((keycode==8)||(keycode==127))
      { character="backspace";  }
    else if((keycode==46))
      { character="delete";   }
    else
      { character=String.fromCharCode(keycode);  }
    return character;
   }

  function fnSanityCheck_A(getdropdown)
  {
    if(OptIndex>(getdropdown.options.length-1))
    {
    alert("PROGRAMMING ERROR: The value of variable vEditableOptionIndex_... cannot be greater than (length of dropdown - 1)");
    return false;
    }
  }

  function fnKeyDown(getdropdown, e)
  {
    fnSanityCheck_A(getdropdown);

    var vEventKeyCode = FindKeyCode(e);

    if( vEventKeyCode == 37 )
      { fnLeftToRight(getdropdown); }
    if( vEventKeyCode == 39 )
      { fnRightToLeft(getdropdown); }

    // Delete key pressed
    if(vEventKeyCode == 46)
    {
      if(getdropdown.options.length != 0)
      // if dropdown is not empty
      {
        if (getdropdown.options.selectedIndex == OptIndex)
        // if option is the Editable field
        {
          getdropdown.options[getdropdown.options.selectedIndex].text = '';
          getdropdown.options[getdropdown.options.selectedIndex].value = '';
        }
      }
    }

    // backspace key pressed
    if(vEventKeyCode == 8 || vEventKeyCode == 127)
    {
      if(getdropdown.options.length != 0)
      // if dropdown is not empty
      {
        if (getdropdown.options.selectedIndex == OptIndex)
        // if option is the Editable field
        {
           // make Editable option Null if it is being edited for the first time
           if ((getdropdown[OptIndex].text == vEditableOptionText_A)||(getdropdown[OptIndex].value == vEditableOptionText_A))
           {
             getdropdown.options[getdropdown.options.selectedIndex].text = '';
             getdropdown.options[getdropdown.options.selectedIndex].value = '';
           }
           else
           {
             getdropdown.options[getdropdown.options.selectedIndex].text = getdropdown.options[getdropdown.options.selectedIndex].text.slice(0,-1);
             getdropdown.options[getdropdown.options.selectedIndex].value = getdropdown.options[getdropdown.options.selectedIndex].value.slice(0,-1);
           }
        }
      }
      if( e.which) //NetscapeFirefoxChrome
        { e.which = ''; }
      else //Internet Explorer
        { e.keyCode = ''; }
      if( e.cancelBubble)   //Internet Explorer
        {
          e.cancelBubble = true;
          e.returnValue = false;
        }
      if( e.stopPropagation)   //NetscapeFirefoxChrome
        { e.stopPropagation();  }
        
      if( e.preventDefault)  //NetscapeFirefoxChrome
        { e.preventDefault();  }
    }
  }

  function fnFocus(getdropdown)
  {
    //use textbox for devices such as android and ipad that don't have a physical keyboard (textbox allows use of virtual soft keyboard)
    if ( (navigator.userAgent.toLowerCase().search(/android|ipad|iphone|ipod/) > -1) || (vUseActualTexbox_A == 'yes') )
    {
      if (getdropdown[(OptIndex)].selected == true)
      {
        getdropdown.style.visibility='';
        getdropdown.style.display='';
      }
      else
      {
        getdropdown.style.visibility='hidden';
        getdropdown.style.display='none';
      }
    }
  }

  function fnChange(getdropdown)
  {
    fnSanityCheck_A(getdropdown);

    vPreviousSelectIndex_A = vSelectIndex_A;
    // Contains the Previously Selected Index

    vSelectIndex_A = getdropdown.options.selectedIndex;
    // Contains the Currently Selected Index

    if ((vPreviousSelectIndex_A == (OptIndex)) && (vSelectIndex_A != (OptIndex))&&(vSelectChange_A != 'MANUAL_CLICK'))
    // To Set value of Index variables - source: http://chakrabarty.com/pp_editable_dropdown.html
    {
      getdropdown[(OptIndex)].selected=true;
      vPreviousSelectIndex_A = vSelectIndex_A;
      vSelectIndex_A = getdropdown.options.selectedIndex;
      vSelectChange_A = 'MANUAL_CLICK';
      // Indicates that the Change in dropdown selected
      // option was due to a Manual Click
    }

    //use textbox for devices such as android and ipad that don't have a physical keyboard (textbox allows use of virtual soft keyboard)
    if ( (navigator.userAgent.toLowerCase().search(/android|ipad|iphone|ipod/) > -1) || (vUseActualTexbox_A == 'yes') )
       {
         fnFocus(getdropdown);
       }
  }

  function fnKeyPress(getdropdown, e)
  {
    fnSanityCheck_A(getdropdown);

    keycode = FindKeyCode(e);
    keychar = FindKeyChar(e);

    if ((keycode>47 && keycode<59)||(keycode>62 && keycode<127) ||(keycode==32))
    {
      var vAllowableCharacter = "yes";
    }
    else
    {
      var vAllowableCharacter = "no";
    }

    if(getdropdown.options.length != 0)
    // if dropdown is not empty
      if (getdropdown.options.selectedIndex == (OptIndex))
      // if selected option the Editable option of the dropdown
      {
        var vEditString = getdropdown[OptIndex].value;

        // make Editable option Null if it is being edited for the first time
        if(vAllowableCharacter == "yes")
        {
          if ((getdropdown[OptIndex].text == vEditableOptionText_A)||(getdropdown[OptIndex].value == vEditableOptionText_A))
            vEditString = "";
        }

        if (vAllowableCharacter == "yes")
        // To handle addition of a character - source: http://chakrabarty.com/pp_editable_dropdown.html
        {
          vEditString+=String.fromCharCode(keycode);
 
          var i=0;
          var vEnteredChar = String.fromCharCode(keycode);
          var vUpperCaseEnteredChar = vEnteredChar;
          var vLowerCaseEnteredChar = vEnteredChar;


          if(((keycode)>=97)&&((keycode)<=122))
          // if vEnteredChar lowercase
            vUpperCaseEnteredChar = String.fromCharCode(keycode - 32);
            // This is UpperCase


          if(((keycode)>=65)&&((keycode)<=90))
          // if vEnteredChar is UpperCase
            vLowerCaseEnteredChar = String.fromCharCode(keycode + 32);
            // This is lowercase

          if(e.which) //For NetscapeFirefoxChrome
          {

            for (i=0;i<=(getdropdown.options.length-1);i++)
            {
              if(i!=OptIndex)
              {
                var vEnteredDigitNumber = getdropdown[OptIndex].text.length;
                var vFirstReadOnlyChar = getdropdown[i].text.substring(0,1);
                var vEquivalentReadOnlyChar = getdropdown[i].text.substring(vEnteredDigitNumber,vEnteredDigitNumber+1);
                if (vEnteredDigitNumber >= getdropdown[i].text.length)
                {
                    vEquivalentReadOnlyChar = vFirstReadOnlyChar;
                }
                if( (vEquivalentReadOnlyChar == vUpperCaseEnteredChar)||(vEquivalentReadOnlyChar == vLowerCaseEnteredChar)
                  ||(vFirstReadOnlyChar == vUpperCaseEnteredChar)||(vFirstReadOnlyChar == vLowerCaseEnteredChar) )
                {
                  vSelectChange_A = 'AUTO_SYSTEM';
                  // Indicates that the Change in dropdown selected
                  // option was due to System properties of dropdown
                  break;
                }
                else
                {
                  vSelectChange_A = 'MANUAL_CLICK';
                  // Indicates that the Change in dropdown selected
                  // option was due to a Manual Click
                }
              }
            }
          }
        }

        // Set the new edited string into the Editable option
        getdropdown.options[OptIndex].text  = vEditString;
        getdropdown.options[OptIndex].value = vEditString;

        return false;
      }
    return true;
  }

  function fnKeyUp(getdropdown, e)
  {
    fnSanityCheck_A(getdropdown);

    if(e.which) // NetscapeFirefoxChrome
    {
      if(vSelectChange_A == 'AUTO_SYSTEM')
      {
        getdropdown[(OptIndex)].selected=true;
        vSelectChange_A = 'MANUAL_CLICK';
      }

      var vEventKeyCode = FindKeyCode(e);
      // if [ <- ] or [ -> ] arrow keys are pressed, select the editable option
      if((vEventKeyCode == 37)||(vEventKeyCode == 39))
      {
        getdropdown[OptIndex].selected=true;
      }
    }
  }
