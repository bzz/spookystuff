package org.tribbloid.spookystuff.entity

import org.tribbloid.spookystuff.{dsl, SpookyEnvSuite}
import org.tribbloid.spookystuff.actions.{Wget, Trace}

/**
 * Created by peng on 12/3/14.
 */
class TestPageRow extends SpookyEnvSuite {

  import dsl._

  test("get page") {
    val page = Trace(
      Wget("http://www.wikipedia.org/") :: Nil
    ).resolve(spooky)
    val row = PageRow(pageLikes = page)

    val page1 = row.getOnlyPage
    assert(page1.get === page.head)

    val page2 = row.getPage("Wget('http://www.wikipedia.org/',true)")
    assert(page2.get === page.head)
  }

  test("get unstructured") {
    val page = Trace(
      Wget("http://www.wikipedia.org/").as('pp) :: Nil
    ).resolve(spooky)
    val row = PageRow(pageLikes = page)
      .select($("h1.central-textlogo img").head.as('e1)).head
      .selectTemp('pp.children("label").head).head

    val page2 = row.getUnstructured("pp")
    assert(page2.get === page.head)

    val e1 = row.getUnstructured("e1")
    assert(e1.get.attr("title").get === "Wikipedia")

    val e2 = row.getUnstructured("pp.children(label).head")
    assert(e2.get.text.get === "Find Wikipedia in a language:")
  }
}