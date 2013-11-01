// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import java.util.ArrayList;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.IOIOFactory;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.api.exception.IncompatibilityException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOConnectionRegistry;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

/**
 *  Class for controlling the pins of the ioio board
 *  Allows for digital input and output and analog input
 *  @author lapentab@mit.edu (Bethany LaPenta)
 */

@UsesLibraries(libraries =
"ioiolibaccessory.jar," +
"ioiolibandroid.jar," +
"ioiolibbt.jar")

@DesignerComponent(version = YaVersion.IOIO_BOARD,
    description = "Sets value to high or low as specified as a digital input to pin.",
    category = ComponentCategory.IOIO_BOARD,
    nonVisible = true,
    iconName = "images/legoMindstormsNxt.png")

@SimpleObject
@UsesPermissions(permissionNames = "android.permission.BLUETOOTH")
public class IOIOPinControl extends AndroidNonvisibleComponent implements Component{
	static {
		IOIOConnectionRegistry.addBootstraps(new String[] {
		    "ioio.lib.android.bluetooth.BluetoothIOIOConnectionBootstrap" });

		}
   public IOIOPinControl(ComponentContainer container) {
	  	super(container.$form());
	}
  
  class Looper extends BaseIOIOLooper {
    DigitalInput[] digitalInput = new DigitalInput[39];
    DigitalOutput[] digitalOutput = new DigitalOutput[39];
    AnalogInput[] analogInput = new AnalogInput[39];
		IOIO ioio = IOIOFactory.create();
		
		// open the digital output port
		public void digitalControlSetup(int port) throws ConnectionLostException{
			try {
				if (ioio.getState()!=IOIO.State.CONNECTED) ioio.waitForConnect();
			} catch (IncompatibilityException e1) {
				e1.printStackTrace();
			}
		    try{
		    	digitalOutput[port] = ioio.openDigitalOutput(port);
		    } catch (Exception e){
		    }

		}
		
		// open the digital input port
    public void digitalInputSetup(int port) throws ConnectionLostException{
      try {
        if (ioio.getState()!=IOIO.State.CONNECTED) ioio.waitForConnect();
      } catch (IncompatibilityException e1) {
        e1.printStackTrace();
      }
        try{
          digitalInput[port] = ioio.openDigitalInput(port);
        } catch (Exception e){
        }

    }
    
    // open the analog input port
    public void analogInputSetup(int port) throws ConnectionLostException{
      try {
        if (ioio.getState()!=IOIO.State.CONNECTED) ioio.waitForConnect();
      } catch (IncompatibilityException e1) {
        e1.printStackTrace();
      }
        try{
          analogInput[port] = ioio.openAnalogInput(port);
        } catch (Exception e){
        }

    }
    
    // Write to a pin
		public void writeTo(boolean isHigh, int pin) throws ConnectionLostException {
			digitalOutput[pin].write(isHigh);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		
		// Get the digital value
    public boolean getValue(int pin) throws ConnectionLostException, InterruptedException {
      Boolean toReturn = digitalInput[pin].read();
      digitalInput[pin].close();
      return toReturn;
    }
    
    // Get the analog value
    public float getValueAnalog(int pin) throws ConnectionLostException, InterruptedException {
      float toReturn = analogInput[pin].read();
      analogInput[pin].close();
      return toReturn;
    }
  }
  Looper l = new Looper();
  /**
   * Writes a digital value to a pin
   */
  @SimpleFunction(description = "Write high or low to a pin")
	  public void SetPinHighOrLow(int port, boolean isHigh) {
		  try{
			  l.digitalControlSetup(port);
			  l.writeTo(isHigh, port);
		  } catch(Exception E){
		  }
	  }
  /**
   * Reads digital value from a pin
   *
   * @return true or false for if the pin is high or low
   */
  @SimpleFunction(description = "Read high or low from a pin")
  public boolean GetPinHighOrLow(int port) {
    try{
      l.digitalInputSetup(port);
      return l.getValue(port);
    } catch(Exception E){
    }
    return false;
  }
  
  /**
   * Reads analog value from a pin
   *
   * @return float the float value of the voltage range (not the voltage itself)
   */
  @SimpleFunction(description = "Read analog values from a pin")
  public float AnalogGetPinHighOrLow(int port) {
    try{
      l.analogInputSetup(port);
      return l.getValueAnalog(port);
    } catch(Exception E){
      E.printStackTrace();
    }
    return 0;
  }
}