// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.IOIOFactory;
import ioio.lib.api.PwmOutput;
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
import com.google.appinventor.components.runtime.util.ErrorMessages;

/**
 * Class for controlling the pins of the ioio board Allows for digital input and output and analog
 * input
 * 
 * @author lapentab@mit.edu (Bethany LaPenta)
 */

@UsesLibraries(libraries = "ioiolibaccessory.jar," + "ioiolibandroid.jar," + "ioiolibbt.jar")
@DesignerComponent(version = YaVersion.IOIO_BOARD,
                   description = "Sets value to high or low as specified as a digital input to pin.",
                   category = ComponentCategory.IOIO_BOARD,
                   nonVisible = true,
                   iconName = "images/ioioBoard.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.BLUETOOTH")
public class IOIOPinControl extends AndroidNonvisibleComponent implements Component {
  static {
    IOIOConnectionRegistry.addBootstraps(new String[] { "ioio.lib.android.bluetooth.BluetoothIOIOConnectionBootstrap" });

  }

  public IOIOPinControl(ComponentContainer container) {
    super(container.$form());
  }

  class Looper extends BaseIOIOLooper {
    DigitalInput[] digitalInput = new DigitalInput[39];
    DigitalOutput[] digitalOutput = new DigitalOutput[39];
    AnalogInput[] analogInput = new AnalogInput[39];
    PwmOutput[] pwmOutput = new PwmOutput[39];
    IOIO ioio = IOIOFactory.create();

    // open the digital output port
    public void digitalControlSetup(int port) throws ConnectionLostException,
            IncompatibilityException {
      if (ioio.getState() != IOIO.State.CONNECTED)
        ioio.waitForConnect();
      try{
      digitalOutput[port] = ioio.openDigitalOutput(port);
      }catch (IllegalArgumentException E){
        // This is if the pin is already open, there's no way to check if it is
        // using the API, so this must be there to catch the error
      }

    }

    // open the digital input port
    public void digitalInputSetup(int port) throws ConnectionLostException,
            IncompatibilityException {
      if (ioio.getState() != IOIO.State.CONNECTED)
        ioio.waitForConnect();
      try{
      digitalInput[port] = ioio.openDigitalInput(port);
      }catch (IllegalArgumentException E){
        // This is if the pin is already open, there's no way to check if it is
        // using the API, so this must be there to catch the error
      }
    }

    // open the analog input port
    public void analogInputSetup(int port) throws ConnectionLostException, IncompatibilityException {
      if (ioio.getState() != IOIO.State.CONNECTED)
        ioio.waitForConnect();
      try{
      analogInput[port] = ioio.openAnalogInput(port);
      }catch (IllegalArgumentException E){
        // This is if the pin is already open, there's no way to check if it is
        // using the API, so this must be there to catch the error
      }


    }

    // Open PWM output port
    public void pwmOutputSetup(int port, int frequency) throws ConnectionLostException,
            IncompatibilityException {
      if (ioio.getState() != IOIO.State.CONNECTED)
        ioio.waitForConnect();
      try{
      pwmOutput[port] = ioio.openPwmOutput(port, frequency);
      }catch (IllegalArgumentException E){
        // This is if the pin is already open, there's no way to check if it is
        // using the API, so this must be there to catch the error
      }

    }

    // Write to a pin
    public void digitalWriteTo(boolean isHigh, int pin) throws ConnectionLostException {
      digitalOutput[pin].write(isHigh);
    }

    // Set PWM Duty Cycle
    public void pwmSetDutyCycle(int pin, int dutycycle) throws ConnectionLostException {
      pwmOutput[pin].setDutyCycle(dutycycle);
    }

    // PWM Set the pulse width
    public void pwmSetPulseWidth(int pin, int pw) throws ConnectionLostException {
      pwmOutput[pin].setPulseWidth(pw);
    }

    // Get the digital value
    public boolean getValueDigital(int pin) throws ConnectionLostException, InterruptedException {
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

  Looper looper = new Looper();

  /**
   * Writes a digital value to a pin
   */
  @SimpleFunction(description = "Write high or low to a pin")
  public void SetDigitalPinHighOrLow(int port, boolean isHigh) {
    try {
      looper.digitalControlSetup(port);
      looper.digitalWriteTo(isHigh, port);
    } catch (ConnectionLostException E) {
      form.dispatchErrorOccurredEvent(this, "Connection Lost", ErrorMessages.ERROR_IOIO_DISCONNECT);
    } catch (IncompatibilityException e) {
      form.dispatchErrorOccurredEvent(this,
              "IOIO Incompatible",
              ErrorMessages.ERROR_IOIO_INCOMPATIBLE);
    }
  }

  /**
   * Reads digital value from a pin
   * 
   * @return true or false for if the pin is high or low
   */
  @SimpleFunction(description = "Read high or low from a pin")
  public boolean GetDigitalPinHighOrLow(int port) {
    try {
      looper.digitalInputSetup(port);
      return looper.getValueDigital(port);
    } catch (ConnectionLostException E) {
      form.dispatchErrorOccurredEvent(this, "Connection Lost", ErrorMessages.ERROR_IOIO_DISCONNECT);
    } catch (InterruptedException e) {
      form.dispatchErrorOccurredEvent(this, "Interrupt", ErrorMessages.ERROR_IOIO_INTERRUPT);
    } catch (IncompatibilityException e) {
      form.dispatchErrorOccurredEvent(this,
              "IOIO Incompatible",
              ErrorMessages.ERROR_IOIO_INCOMPATIBLE);
    }
    return false;
  }

  /**
   * Reads analog value from a pin
   * 
   * @return float the float value of the voltage range (not the voltage itself)
   */
  @SimpleFunction(description = "Read analog values from a pin")
  public float GetAnalogPinHighOrLow(int port) {
    try {
      looper.analogInputSetup(port);
      return looper.getValueAnalog(port);
    } catch (ConnectionLostException E) {
      form.dispatchErrorOccurredEvent(this, "Connection Lost", ErrorMessages.ERROR_IOIO_DISCONNECT);
      E.printStackTrace();
    } catch (InterruptedException e) {
      form.dispatchErrorOccurredEvent(this, "Interrupt", ErrorMessages.ERROR_IOIO_INTERRUPT);
    } catch (IncompatibilityException e) {
      form.dispatchErrorOccurredEvent(this,
              "IOIO Incompatible",
              ErrorMessages.ERROR_IOIO_INCOMPATIBLE);
    }
    return 0;
  }

  /**
   * Writes a PWM signal with set frequency and duty cycle
   * 
   */
  @SimpleFunction(description = "Sets Pulse Width with a Duty Cycle and frequency")
  public void SetPWMDutyCycle(int port, int frequency, int dutycycle) {
    try {
      looper.pwmOutputSetup(port, frequency);
      looper.pwmSetDutyCycle(port, dutycycle);
    } catch (ConnectionLostException E) {
      form.dispatchErrorOccurredEvent(this, "Connection Lost", ErrorMessages.ERROR_IOIO_DISCONNECT);
    } catch (IncompatibilityException e) {
      form.dispatchErrorOccurredEvent(this,
              "IOIO Incompatible",
              ErrorMessages.ERROR_IOIO_INCOMPATIBLE);
    }
  }

  /**
   * Writes a PWM signal with set frequency
   * 
   */
  @SimpleFunction(description = "Sets Pulse Width with frequency")
  public void SetPWMFrequency(int port, int frequency) {
    try {
      looper.pwmOutputSetup(port, frequency);
    } catch (ConnectionLostException E) {
      form.dispatchErrorOccurredEvent(this, "Connection Lost", ErrorMessages.ERROR_IOIO_DISCONNECT);
    } catch (IncompatibilityException e) {
      form.dispatchErrorOccurredEvent(this,
              "IOIO Incompatible",
              ErrorMessages.ERROR_IOIO_INCOMPATIBLE);
    }
  }
}