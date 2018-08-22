package com.github.freeacs.domain
import com.github.freeacs.domain.ACSUnitType.UnitTypeDescription.UnitTypeDescription
import com.github.freeacs.domain.ACSUnitType.UnitTypeId.UnitTypeId
import com.github.freeacs.domain.ACSUnitType.UnitTypeMatcherId.UnitTypeMatcherId
import com.github.freeacs.domain.ACSUnitType.UnitTypeName.UnitTypeName
import com.github.freeacs.domain.ACSUnitType.UnitTypeProtocol.UnitTypeProtocol
import com.github.freeacs.domain.ACSUnitType.UnitTypeVendorName.UnitTypeVendorName
import shapeless.tag.@@

final case class ACSUnitType(
    unitTypeName: UnitTypeName,
    protocol: UnitTypeProtocol,
    unitTypeId: Option[UnitTypeId] = None,
    matcherId: Option[UnitTypeMatcherId] = None,
    vendorName: Option[UnitTypeVendorName] = None,
    description: Option[UnitTypeDescription] = None,
    params: Seq[ACSUnitTypeParameter] = Seq.empty
)

object ACSUnitType {

  def fromResultSet(
      unitTypeName: String,
      protocol: String,
      unitTypeId: Option[Long],
      matcherId: Option[String],
      vendorName: Option[String],
      description: Option[String]
  ) =
    ACSUnitType(
      UnitTypeName(unitTypeName),
      UnitTypeProtocol(protocol),
      unitTypeId.map(UnitTypeId.apply),
      matcherId.map(UnitTypeMatcherId.apply),
      vendorName.map(UnitTypeVendorName.apply),
      description.map(UnitTypeDescription.apply)
    )

  object UnitTypeId {
    trait Tag
    type UnitTypeId = Long @@ Tag

    def apply(v: Long): UnitTypeId =
      shapeless.tag[Tag][Long](v)
  }

  object UnitTypeName {
    trait Tag
    type UnitTypeName = String @@ Tag

    def apply(v: String): UnitTypeName =
      shapeless.tag[Tag][String](v)
  }

  object UnitTypeProtocol {
    trait Tag
    type UnitTypeProtocol = String @@ Tag

    def apply(v: String): UnitTypeProtocol =
      shapeless.tag[Tag][String](v)
  }

  object UnitTypeMatcherId {
    trait Tag
    type UnitTypeMatcherId = String @@ Tag

    def apply(v: String): UnitTypeMatcherId =
      shapeless.tag[Tag][String](v)
  }

  object UnitTypeVendorName {
    trait Tag
    type UnitTypeVendorName = String @@ Tag

    def apply(v: String): UnitTypeVendorName =
      shapeless.tag[Tag][String](v)
  }

  object UnitTypeDescription {
    trait Tag
    type UnitTypeDescription = String @@ Tag

    def apply(v: String): UnitTypeDescription =
      shapeless.tag[Tag][String](v)
  }
}
