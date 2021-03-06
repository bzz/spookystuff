package org.tribbloid.spookystuff.dsl

import scala.util.Random

/**
 * Created by peng on 11/4/14.
 */
case class ProxySetting (
                       addr: String,
                       port: Int,
                       protocol: String
                       ) {

}

abstract class ProxyFactory extends (() => ProxySetting) with Serializable

object NoProxyFactory extends ProxyFactory {
  override def apply(): ProxySetting = null
}

object TorProxyFactory extends ProxyFactory {
  def apply() = ProxySetting("127.0.0.1", 9050, "socks5")
}

case class RandomProxyFactory(proxies: Seq[ProxySetting]) extends ProxyFactory {
  def apply() = proxies(Random.nextInt(proxies.size))
}