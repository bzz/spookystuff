package org.tribbloid.spookystuff.example

import org.apache.spark.sql.{SQLContext, SchemaRDD}
import org.apache.spark.{SparkConf, SparkContext}
import org.tribbloid.spookystuff.{SpookyConf, SpookyContext}
import org.tribbloid.spookystuff.dsl.NaiveDriverFactory

import scala.concurrent.duration
import duration._

/**
 * Created by peng on 22/06/14.
 * allowing execution as a main object and tested as a test class
 * keep each test as small as possible, by using downsampling & very few iterations
 */
trait ExampleCore extends {

  val appName = this.getClass.getSimpleName.replace("$","")
  val sc: SparkContext = {
    val conf: SparkConf = new SparkConf().setAppName(appName)

    var master: String = null
    master = Option(master).getOrElse(conf.getOption("spark.master").orNull)
    master = Option(master).getOrElse(System.getenv("MASTER"))
    master = Option(master).getOrElse("local[8,3]")

    conf.setMaster(master)
    new SparkContext(conf)
  }

  override def finalize(): Unit = {
    sc.stop()
  }

  val sql: SQLContext = {

    new SQLContext(sc)
  }

  def getSpooky(args: Array[String]): SpookyContext = {

    val spooky = if (args.contains("--nopreview"))
      new SpookyContext(sql)
    else
      new SpookyContext(
        sql,
        new SpookyConf(
          driverFactory = NaiveDriverFactory(loadImages = true),
          maxJoinOrdinal = 2,
          maxExploreDepth = 3
        )
      )

    if (!args.contains("--dfscache")) spooky.conf.dirs.setRoot("file://"+System.getProperty("user.home")+"/spooky-example/"+appName+"/")

    if (args.contains("--nocache")) spooky.conf.cacheRead = false

    spooky
  }

  def doMain(spooky: SpookyContext): SchemaRDD

  final def main(args: Array[String]) {

    val spooky = getSpooky(args)

    val result = doMain(spooky).persist()

    val array = result.collect()
    array.foreach(row => println(row.mkString("\t")))

    println("-------------------returned "+array.length+" rows------------------")
    println(result.schema.fieldNames.mkString("\t"))
    println(s"------------------metrics-----------------")
    println(spooky.metrics.toJSON)
  }
}