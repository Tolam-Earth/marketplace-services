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

package com.tolamearth.marketplace.common;

import java.util.function.Supplier;

/**
 * Merely holds two values and has a method to compare them. Named in a way readable for testing and
 * uses generics.
 */
public class AssertionPair<T> {

  private final Supplier<T> expected;
  private final Supplier<T> actual;

  public AssertionPair(Supplier<T> expected, Supplier<T> actual) {
    this.expected = expected;
    this.actual = actual;
  }

  public AssertionPair(T expected, T actual) {
    this.expected = () -> expected;
    this.actual = () -> actual;
  }

  public static <T> AssertionPair<T> of(T expected, T actual) {
    return new AssertionPair<>(expected, actual);
  }

  public static <T> AssertionPair<T> of(Supplier<T> expected, T actual) {
    return new AssertionPair<>(expected, () -> actual);
  }

  public static <T> AssertionPair<T> of(T expected, Supplier<T> actual) {
    return new AssertionPair<>(() -> expected, actual);
  }

  public T expected() {
    return expected.get();
  }

  public T actual() {
    return actual.get();
  }
}
