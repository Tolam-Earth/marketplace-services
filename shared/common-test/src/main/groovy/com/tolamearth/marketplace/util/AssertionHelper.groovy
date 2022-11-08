/*
 * Copyright 2022 Tolam Earth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tolamearth.marketplace.util

import org.junit.jupiter.api.function.Executable

class AssertionHelper {

  static List<Executable> assertions(List<Closure> closures) {
    convertAssertions(closures)
  }

  static List<Executable> assertions(Closure... closures) {
    convertAssertions(closures.toList())
  }

  private static List<Executable> convertAssertions(List<Closure> closures) {
    closures.collect({
      it as Executable
    })
  }
}
