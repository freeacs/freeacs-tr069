package com.github.freeacs.config

object SystemParameters {
  /**
    * Control provisioning (secret, encryption, download, reboot, reset) through these parameters
    */
  // The desired config version for a unit
  val DESIRED_TR069_SCRIPT = "System.X_FREEACS-COM.TR069Script."
  // The desired software/firmware version for a unit
  val DESIRED_SOFTWARE_VERSION = "System.X_FREEACS-COM.DesiredSoftwareVersion"
  // The software/firmware url (used in download request)
  val SOFTWARE_URL = "System.X_FREEACS-COM.SoftwareURL"
  // The secret, both for TR-069 and OPP. Will replace both TR069_SECRET and OPP_SECRET
  val SECRET = "System.X_FREEACS-COM.Secret"
  // A scheme to be used to encrypt the reponse from the server - probably only used for TFTP
  val SECRET_SCHEME = "System.X_FREEACS-COM.SecretScheme"
  // The reboot flag, set to reboot the Device
  val RESTART = "System.X_FREEACS-COM.Restart"
  // The reset flag, set to factory reset the Device
  val RESET = "System.X_FREEACS-COM.Reset"
  // The discovery flag, set to run GetParameterNames on device - update the unittype configuration
  val DISCOVER = "System.X_FREEACS-COM.Discover"
  val COMMENT = "System.X_FREEACS-COM.Comment"
  /**
    * Data about the provisioning client connect/provisioning
    */
  val FIRST_CONNECT_TMS = "System.X_FREEACS-COM.FirstConnectTms"
  val LAST_CONNECT_TMS = "System.X_FREEACS-COM.LastConnectTms"
  /**
    * The provisioning mode and state parameter, use the MODE_ constants and STATE_ constants
    */
  val PROVISIONING_MODE = "System.X_FREEACS-COM.ProvisioningMode"
  val INSPECTION_MESSAGE = "System.X_FREEACS-COM.IM.Message"
  /**
    * The service window parameters regulate when a provisioning of a certain type can be performed
    */
  val SERVICE_WINDOW_ENABLE = "System.X_FREEACS-COM.ServiceWindow.Enable"
  val SERVICE_WINDOW_REGULAR = "System.X_FREEACS-COM.ServiceWindow.Regular"
  val SERVICE_WINDOW_DISRUPTIVE = "System.X_FREEACS-COM.ServiceWindow.Disruptive"
  val SERVICE_WINDOW_FREQUENCY = "System.X_FREEACS-COM.ServiceWindow.Frequency"
  val SERVICE_WINDOW_SPREAD = "System.X_FREEACS-COM.ServiceWindow.Spread"
  /**
    * The conversation log parameter, enables an operator to turn on conversation logging on certain units to a special debug-file (in TR-069 server)
    */
  val DEBUG = "System.X_FREEACS-COM.Debug"
  /**
    * The current job and the history of jobs
    */
  val JOB_CURRENT = "System.X_FREEACS-COM.Job.Current"
  val JOB_CURRENT_KEY = "System.X_FREEACS-COM.Job.CurrentKey"
  val JOB_HISTORY = "System.X_FREEACS-COM.Job.History"
  val JOB_DISRUPTIVE = "System.X_FREEACS-COM.Job.Disruptive"
  /**
    * Device parameters/info - stored under System parameter for cross-protocol compatibility
    */
  val SERIAL_NUMBER = "System.X_FREEACS-COM.Device.SerialNumber"
  val SOFTWARE_VERSION = "System.X_FREEACS-COM.Device.SoftwareVersion"
  val PERIODIC_INTERVAL = "System.X_FREEACS-COM.Device.PeriodicInterval"
  val IP_ADDRESS = "System.X_FREEACS-COM.Device.PublicIPAddress"
  val PROTOCOL = "System.X_FREEACS-COM.Device.PublicProtocol"
  val PORT = "System.X_FREEACS-COM.Device.PublicPort"
  val GUI_URL = "System.X_FREEACS-COM.Device.GUIURL"

  var COMMON_PARAMETERS = Map[String, String](
    DESIRED_SOFTWARE_VERSION -> "X",
    SOFTWARE_URL -> "X",
    PROVISIONING_MODE -> "X",
    INSPECTION_MESSAGE -> "X",
    SERVICE_WINDOW_ENABLE -> "X",
    SERVICE_WINDOW_REGULAR -> "X",
    SERVICE_WINDOW_DISRUPTIVE -> "X",
    SERVICE_WINDOW_FREQUENCY -> "X",
    SERVICE_WINDOW_SPREAD -> "X",
    DEBUG -> "X",
    JOB_CURRENT -> "X",
    JOB_CURRENT_KEY -> "X",
    JOB_HISTORY -> "X",
    JOB_DISRUPTIVE -> "X",
    FIRST_CONNECT_TMS -> "X",
    LAST_CONNECT_TMS -> "X",
    SERIAL_NUMBER -> "X",
    RESTART -> "X",
    RESET -> "X",
    DISCOVER -> "X",
    COMMENT -> "X",
    SECRET -> "XC",
    SECRET_SCHEME -> "X",
    SOFTWARE_VERSION -> "X",
    PERIODIC_INTERVAL -> "X",
    IP_ADDRESS -> "X",
    PROTOCOL -> "X",
    PORT -> "X",
    GUI_URL -> "X"
  )
}