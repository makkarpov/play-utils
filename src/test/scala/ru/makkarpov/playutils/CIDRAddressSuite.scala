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

import org.scalatest.{Matchers, WordSpec}

class CIDRAddressSuite extends WordSpec with Matchers {
  "CIDRAddress" must {
    "match IPv4 addresses" in {
      val loopback = CIDRAddress("127.128.129.130/8")
      loopback.start.getHostAddress shouldBe "127.0.0.0"
      loopback.end.getHostAddress shouldBe "127.255.255.255"
      loopback.contains("127.0.12.3") shouldBe true
      loopback.contains("127.255.255.255") shouldBe true
      loopback.contains("128.0.0.0") shouldBe false
      loopback.contains("126.255.255.255") shouldBe false

      val internet = CIDRAddress("0.0.0.0/0")
      internet.start.getHostAddress shouldBe "0.0.0.0"
      internet.end.getHostAddress shouldBe "255.255.255.255"
      internet.contains("0.0.0.0") shouldBe true
      internet.contains("255.255.255.255") shouldBe true
      internet.contains("127.0.0.1") shouldBe true
      internet.contains("192.168.1.1") shouldBe true

      val singleIp = CIDRAddress("10.1.2.3")
      singleIp.start.getHostAddress shouldBe "10.1.2.3"
      singleIp.end.getHostAddress shouldBe "10.1.2.3"
      singleIp.contains("10.1.2.3") shouldBe true
      singleIp.contains("10.1.2.4") shouldBe false
    }

    "fail on invalid IPv4 inputs" in {
      an [IllegalArgumentException] shouldBe thrownBy { CIDRAddress("0.0.0.0/-1") }
      an [IllegalArgumentException] shouldBe thrownBy { CIDRAddress("0.0.0.0/33") }
      an [IllegalArgumentException] shouldBe thrownBy { CIDRAddress("0.0.0.0/x") }
      an [IllegalArgumentException] shouldBe thrownBy { CIDRAddress("0.0.0.0/") }
      an [IllegalArgumentException] shouldBe thrownBy { CIDRAddress("0.0.0.256") }
      an [IllegalArgumentException] shouldBe thrownBy { CIDRAddress("0.0.0.") }
      an [IllegalArgumentException] shouldBe thrownBy { CIDRAddress("") }
    }

    "match IPv6 addresses" in {
      val test = CIDRAddress("a:b:c:d::/64")
      test.start.getHostAddress shouldBe "a:b:c:d:0:0:0:0"
      test.end.getHostAddress shouldBe "a:b:c:d:ffff:ffff:ffff:ffff"
      test.contains("a:b:c:d:1234:1488:1337:1234") shouldBe true
      test.contains("a:b:c:e::") shouldBe false
      test.contains("a:b:c:c:ffff:ffff:ffff:ffff") shouldBe false

      val internet = CIDRAddress("::/0")
      internet.start.getHostAddress shouldBe "0:0:0:0:0:0:0:0"
      internet.end.getHostAddress shouldBe "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"
      internet.contains("1:2:3:4:5:6:7:8") shouldBe true
      internet.contains("f:e:d:c:b:a:9:8") shouldBe true

      val singleIp = CIDRAddress("::1")
      singleIp.start.getHostAddress shouldBe "0:0:0:0:0:0:0:1"
      singleIp.end.getHostAddress shouldBe "0:0:0:0:0:0:0:1"
    }

    "fail on invalid IPv6 inputs" in {
      an [IllegalArgumentException] shouldBe thrownBy { CIDRAddress("::/-1") }
      an [IllegalArgumentException] shouldBe thrownBy { CIDRAddress("::/129") }
      an [IllegalArgumentException] shouldBe thrownBy { CIDRAddress("::/x") }
      an [IllegalArgumentException] shouldBe thrownBy { CIDRAddress("::/") }
      an [IllegalArgumentException] shouldBe thrownBy { CIDRAddress("a::b::c") }
      an [IllegalArgumentException] shouldBe thrownBy { CIDRAddress("fffff::") }
    }

    "reject IPv4/6 mixes" in {
      CIDRAddress("::1/64").contains("127.0.0.1") shouldBe false
      CIDRAddress("127.0.0.0/8").contains("::1") shouldBe false
    }

    "generate well-formed toString" in {
      CIDRAddress("0:0:0:0:0:0:0:0/128").toString shouldBe "::"
      CIDRAddress("f:f:f:f:f:f:f:f/16").toString shouldBe "f::/16"
      CIDRAddress("127.255.255.255/8").toString shouldBe "127.0.0.0/8"
    }
  }
}
