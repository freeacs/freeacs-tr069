package com.github.freeacs.entities

final case class DeviceIdStruct(
  manufacturer: String,
  oui: String,
  productClass: String,
  serialNumber: String
)
