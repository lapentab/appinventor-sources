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
import ioio.lib.api.TwiMaster;
import ioio.lib.api.TwiMaster.Rate;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.api.exception.IncompatibilityException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOConnectionRegistry;
import android.util.Log;

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
  // Motor Ports, can be changed
  int portFR = 10;
  int portFL = 11;
  int portBR = 12;
  int portBL = 13;
  int portUSSensor1 = 36;
  // Motor Frequency, can be changed.
  int motorFrequency = 50;
  
  // Light sensor port
  int portLuminosityTwi = 1;

         
  public IOIOPinControl(ComponentContainer container) {
    super(container.$form());
  }

  class Looper extends BaseIOIOLooper {
    DigitalInput[] digitalInput = new DigitalInput[39];
    DigitalOutput[] digitalOutput = new DigitalOutput[39];
    AnalogInput[] analogInput = new AnalogInput[39];
    PwmOutput[] pwmOutput = new PwmOutput[39];
    TwiMaster[] twiMaster = new TwiMaster[3];
    IOIO ioio = IOIOFactory.create();

    // open the twiMaster port
    public void twiMasterSetup(int port) throws ConnectionLostException,
            IncompatibilityException {
      if (ioio.getState() != IOIO.State.CONNECTED)
        ioio.waitForConnect();
      try{
        Log.d("IOIODebug", "Opening twi port");
        twiMaster[port] = ioio.openTwiMaster(port, TwiMaster.Rate.RATE_100KHz, false);        
      }catch (IllegalArgumentException E){
        Log.d("IOIODebug", "Opened problem " + E.getMessage());
        // This is if the pin is already open, there's no way to check if it is
        // using the API, so this must be there to catch the error
      }
    }
   
     public void initLightSensor(int port){
       Log.d("IOIODebug", "Init light sensor on port " + port);
       InitTSL2561(0x39, twiMaster[port]);
     }
     
    
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
    public void pwmSetDutyCycle(int pin, float dutycycle) throws ConnectionLostException {
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
    // light sensor

    
    /*
     * 
     * The TSL2651 code is pasted/modified from here: http://www.briandorey.com/post/IOIO-Android-I2C-Temperature-Logging-Code.aspx
     */
    public boolean InitTSL2561(int address, TwiMaster port) {
      byte[] request_on = new byte[] { 0x0A };
      byte[] response = new byte[4];
      try {
        try {
          if (port.writeRead(address, false, request_on, request_on.length, response, response.length)) {
            byte[] request_setGain = new byte[] { (byte) 0x80, 0x01, 0x00 };

            try {
              if (port.writeRead(address,
                      false,
                      request_setGain,
                      request_setGain.length,
                      response,
                      response.length)) {
                return true;
              }
            } catch (ConnectionLostException e) {
              e.printStackTrace();
            }

          } else {
            return false;
          }
        } catch (ConnectionLostException e) {
          e.printStackTrace();
        }
      } catch (InterruptedException e) {
        return false;
      }
      return false;
    }

    public int[] ReadTSL2561(int port,int address) {
      TwiMaster twi = twiMaster[port];
      int channel0 = 99;
      int channel1 = 99;

      byte[] DataHigh = new byte[1];
      byte[] DataLow = new byte[1];
      byte[] DataHigh2 = new byte[1];
      byte[] DataLow2 = new byte[1];
      byte[] response = new byte[4];
      int[] returnval = new int[2];

      try {
        // init sensor
        byte[] request_on = new byte[] { 0x03 };
        Log.d("IOIODebug", "" + address);
        Log.d("IOIODebug", "" + request_on);
        Log.d("IOIODebug", "" + response);
        Log.d("IOIODebug", twi.toString());
        twi.writeRead(address, false, request_on, request_on.length, response, response.length);

        // get low channel
        byte[] request_2a = new byte[] { (byte) 0x8C };
        twi.writeRead(address, false, request_2a, request_2a.length, DataLow, DataLow.length);

        byte[] request_2b = new byte[] { (byte) 0x8D };
        twi.writeRead(address, false, request_2b, request_2b.length, DataHigh, DataHigh.length);

        // get high channel
        byte[] request_higha = new byte[] { (byte) 0x8E };
        twi.writeRead(address, false, request_higha, request_higha.length, DataLow2, DataLow2.length);

        byte[] request_highb = new byte[] { (byte) 0x8F };
        twi.writeRead(address,
                false,
                request_highb,
                request_highb.length,
                DataHigh2,
                DataHigh2.length);

        channel0 = ((0xFF & (int) DataHigh[0]) * 256) + ((0xFF & (int) DataLow[0]));
        channel1 = ((0xFF & (int) DataHigh2[0]) * 256) + ((0xFF & (int) DataLow2[0]));
        returnval[0] = channel0;
        returnval[1] = channel1;
        // close request
        byte[] request_off = new byte[] { 0x00 };
        twi.writeRead(address, false, request_off, request_off.length, response, response.length);
        return returnval;
      } catch (ConnectionLostException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } finally {
      }

      return returnval;
    }

    public int[] bytearray2intarray(byte[] barray) {
      int[] iarray = new int[barray.length];
      int i = 0;
      for (byte b : barray)
        iarray[i++] = b & 0xff;
      // "and" with 0xff since bytes are signed in java
      return iarray;
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
  @SimpleFunction(description = "Sets PWM signal's Duty Cycle")
  public void SetPWMDutyCycle(int port, float dutycycle) {
    try {
      looper.pwmSetDutyCycle(port, dutycycle);
    } catch (ConnectionLostException E) {
      form.dispatchErrorOccurredEvent(this, "Connection Lost", ErrorMessages.ERROR_IOIO_DISCONNECT);
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
   // Sets up a port as a light sensor, must already be set up as a TWI sensor
  public void setupLightSensor(int port){
    try{
    looper.twiMasterSetup(port);
    looper.initLightSensor(port);
    } catch (ConnectionLostException E) {
      form.dispatchErrorOccurredEvent(this, "Connection Lost", ErrorMessages.ERROR_IOIO_DISCONNECT);
    } catch (IncompatibilityException e) {
      form.dispatchErrorOccurredEvent(this,
              "IOIO Incompatible",
              ErrorMessages.ERROR_IOIO_INCOMPATIBLE);
    }
  }
  /**
   * Reads visible light
   * 
   * @return int the value of the sensor
   */
  public float getVisibleLight(int port) {
    int lightsensors[] = looper.ReadTSL2561(port, 0x39);
    int visible = lightsensors[0];
    return visible;
  }
  
  /**
   * Reads IR light
   * 
   * @return int the value of the sensor
   */
  public float getIRLight(int port) {
    int lightsensors[] = looper.ReadTSL2561(port, 0x39);
    int irlight = lightsensors[1];
    return irlight;
  }
  
  
  
  // Internal method to set up motor frequency
  public void setupMotors(){
    SetPWMFrequency(portFR, motorFrequency);
    SetPWMFrequency(portFL, motorFrequency);
    SetPWMFrequency(portBR, motorFrequency);
    SetPWMFrequency(portBL, motorFrequency);
    setupLightSensor(portLuminosityTwi);
  }
  // Internal method to set motor's duty cycles
  public void setMotors(double FRDuty, double FLDuty, double BRDuty, double BLDuty){
    SetPWMDutyCycle(portFR, (float)FRDuty);
    SetPWMDutyCycle(portFL, (float)FLDuty);
    SetPWMDutyCycle(portBR, (float)BRDuty);
    SetPWMDutyCycle(portBL, (float)BLDuty);
  }
  
  /**
   * Sets up motors for use, setting the frequency.
   */
  @SimpleFunction(description = "Initialize Motors. Put this in Screen.Initialize, must be called before using motors")
  public void SetupMotors() {
    setupMotors();
  }
  
  /**
   * Moves Forward.
   */
  @SimpleFunction(description = "Move Robot Forward")
  public void MoveRobotForward() {
    setMotors(.1, .1, .1, .1);
  }
  
  /**
   * Moves Backwards.
   */
  @SimpleFunction(description = "Move Robot Backwards")
  public void MoveRobotBackwards() {
    setMotors(.05, .05, .05, .05);
  }
  
  /**
   * Stops the Robot
   */
  @SimpleFunction(description = "Stop the robot completely.")
  public void StopRobot() {
    setMotors(.075, .075, .075, .075);
  }
  
  
  /**
   *  Turns robot to left
   */
  @SimpleFunction(description = "Turn the robot to the left")
  public void TurnRobotLeft() {
    setMotors(.1, .05, .1, .05);
  }
 
  /**
   *  Turns robot to right
   */
  @SimpleFunction(description = "Turn the robot to the right")
  public void TurnRobotRight() {
    setMotors(.05, .1, .05, .1);
  }
  
  /**
   *  Get visible light from luminosity sensor
   */
  @SimpleFunction(description = "Return visible light level of luminosity sensor")
  public float GetVisibleLight() {
    return getVisibleLight(portLuminosityTwi);
  }
  
  
  /**
   *  Get IR light from luminosity sensor
   */
  @SimpleFunction(description = "Return IR light level of luminosity sensor")
  public float GetIRLight() {
    return getIRLight(portLuminosityTwi);
  }
  
  /**
   *  Get Ultrasonic Data
   */
  @SimpleFunction(description = "Return Ultrasonic Range")
  public float GetUltrasonicRange() {
    return GetAnalogPinHighOrLow(portUSSensor1);
  }
}