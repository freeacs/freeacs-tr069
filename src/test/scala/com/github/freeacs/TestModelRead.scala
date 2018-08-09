package com.github.freeacs

import broadbandforumorg.cwmp.datamodel16.{
  AllBuiltinDataTypesOption,
  CommandObject,
  StringType,
  UnsignedInt
}
import scalaxb.`package`.fromXML

import scala.xml.{Source, XML}

object TestModelRead extends App {

  def doShit(file: String): Unit = {
    val is =
      Source.fromInputStream(getClass.getClassLoader.getResourceAsStream(file))
    val xml = XML.load(is)
    (xml \\ "object").seq
      .map(fromXML[CommandObject](_))
      .headOption
      .foreach(obj => {
        println(obj.name)
        println("<<<--------")
        obj.parameter.foreach(p => {
          println(p.name)
          p.syntax
            .map(s => s.syntaxoption2.value)
            .filter(_.isInstanceOf[AllBuiltinDataTypesOption])
            .map(_.asInstanceOf[AllBuiltinDataTypesOption])
            .foreach {
              case int: UnsignedInt =>
                println(int)
              case string: StringType =>
                println(string)
              case _ =>
                println()
            }
        })
        println("-------->>>")
      })
  }

  doShit("tr-181-2-12-0-cwmp-full.xml")
  doShit("tr-104-2-0-0-full.xml")
  doShit("tr-098-1-8-0-full.xml")
}
