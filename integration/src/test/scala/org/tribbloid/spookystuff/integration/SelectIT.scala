package org.tribbloid.spookystuff.integration

import java.text.SimpleDateFormat

import org.tribbloid.spookystuff.SpookyContext
import org.tribbloid.spookystuff.actions._
import org.tribbloid.spookystuff.dsl._

/**
 * Created by peng on 11/26/14.
 */
class SelectIT extends IntegrationSuite {

  override def doMain(spooky: SpookyContext) {

    import spooky._

    val pageRowRDD = noInput
      .fetch(
        Visit("http://www.wikipedia.org/")
      )
      .select(
        $.uri,
        $.timestamp,
        $"div.central-featured-lang em".text ~ 'title,
        $"div.central-featured-lang strong".texts ~ 'langs
      )
      .persist()

      val RDD = pageRowRDD
      .toSchemaRDD()

    assert(
      RDD.schema.fieldNames ===
        "$_uri" ::
          "$_timestamp" ::
          "title" ::
          "langs" :: Nil
    )

    val rows = RDD.collect()
    val finishTime = System.currentTimeMillis()
    assert(rows.size === 1)
    assert(rows.head.size === 4)
    assert(rows.head.getString(0) === "http://www.wikipedia.org/")
    val parsedTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(rows.head.getString(1)).getTime
    assert(parsedTime < finishTime +2000) //due to round-off error
    assert(parsedTime > finishTime-60000) //long enough even after the second time it is retrieved from the cache
    val title = rows.head.getString(2)
    assert(title === "The Free Encyclopedia")
    val langs = rows.head(3).asInstanceOf[Iterable[String]]
    assert(langs.size === 10)
    assert(langs.head === "English")

    intercept[AssertionError] {
      pageRowRDD.select(
        $"div.central-featured-lang strong".text ~ 'title
      )
    }

    val RDD2 = pageRowRDD
      .select(
        $"div.central-featured-lang strong".text ~+ 'title
      )
      .toSchemaRDD()

    assert(
      RDD2.schema.fieldNames ===
        "$_uri" ::
          "$_timestamp" ::
          "title" ::
          "langs" :: Nil
    )

    val rows2 = RDD2.collect()
    val titles = rows2.head(2).asInstanceOf[Iterable[String]]
    assert(titles === Seq("The Free Encyclopedia", "English"))
  }

  override def numPages = _ => 1
}