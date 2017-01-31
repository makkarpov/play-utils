/******************************************************************************
 * Copyright © 2017 Maxim Karpov                                              *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 ******************************************************************************/

package ru.makkarpov.playutils

import java.net.{Inet4Address, Inet6Address, InetAddress}

import com.google.common.net.InetAddresses

object CIDRAddress {
  def apply(x: String) = new CIDRAddress(x)
}

class CIDRAddress(_address: String) {
  val (address, prefix) = _address.split("/", 2) match {
    case Array(ip, sprefix) =>
      val prefix = sprefix.toInt
      val addr = InetAddresses.forString(ip)

      if (prefix > maxPrefix(addr) || prefix < 0)
        throw new IllegalArgumentException(s"Incorrect prefix $prefix for address '$addr'")
      else
        addr -> prefix

    case Array(ip) =>
      val addr = InetAddresses.forString(ip)
      addr -> maxPrefix(addr)
  }

  val (start, end) = {
    val mask = (BigInt(2).pow(prefix) - 1) << (maxPrefix(address) - prefix)
    val invMask = BigInt(2).pow(maxPrefix(address) - prefix) - 1
    val base = BigInt(1, address.getAddress)

    val start = padBytes((base & mask).toByteArray, maxPrefix(address))
    val end = padBytes((base | invMask).toByteArray, maxPrefix(address))

    InetAddress.getByAddress(start) -> InetAddress.getByAddress(end)
  }

  private val startInt = BigInt(1, start.getAddress)
  private val endInt = BigInt(1, end.getAddress)

  private def maxPrefix(addr: InetAddress): Int = addr match {
    case _: Inet4Address => 32
    case _: Inet6Address => 128
  }

  private def padBytes(x: Array[Byte], bitLen: Int): Array[Byte] = {
    val byteLen = bitLen / 8

    if (x.length == byteLen) x
    else {
      val ret = new Array[Byte](byteLen)
      val retLength = math.min(x.length, byteLen)
      System.arraycopy(x, math.max(0, x.length - byteLen), ret, byteLen - retLength, retLength)
      ret
    }
  }

  @inline
  def contains(x: String): Boolean = contains(InetAddresses.forString(x))

  def contains(x: InetAddress): Boolean = {
    if (x.isInstanceOf[Inet4Address] != start.isInstanceOf[Inet4Address])
      return false

    val target = BigInt(1, x.getAddress)
    (target >= startInt) && (target <= endInt)
  }

  override def toString: String = {
    val addr = InetAddresses.toAddrString(start)
    if (prefix == maxPrefix(address)) addr
    else s"$addr/$prefix"
  }
}
