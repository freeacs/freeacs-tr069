package com.github.freeacs.xml

import java.net.URLDecoder

final case class DeviceIdStruct(
    manufacturer: String,
    oui: String,
    productClass: String,
    serialNumber: String
)
