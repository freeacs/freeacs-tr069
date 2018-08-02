package com.github.freeacs.xml

import java.net.URLDecoder

final case class DeviceIdStruct(
  manufacturer: String,
  oui: String,
  productClass: String,
  serialNumber: String
) {
  val unitId: String = {
    var unitId: String = null
    if (productClass != null && !productClass.trim.equals("")) {
      unitId = s"$oui-$productClass-$serialNumber"
    } else {
      unitId = s"$oui-$serialNumber"
    }
    URLDecoder.decode(unitId, "UTF-8")
  }
}
