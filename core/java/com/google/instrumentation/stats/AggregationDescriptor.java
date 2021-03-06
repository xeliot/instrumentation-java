/*
 * Copyright 2016, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.instrumentation.stats;

import com.google.instrumentation.common.Duration;
import com.google.instrumentation.common.Function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/**
 * An {@link AggregationDescriptor} describes an Aggregation, which is a series of individual
 * measurements.
 *
 * <p> Each measurments can be one of either:
 * <ul>
 * <li> {@link DistributionAggregationDescriptor}
 * <li> {@link IntervalAggregationDescriptor}
 * </ul>
 */
public abstract class AggregationDescriptor {
  /**
   * Applies the given match function to the underlying data type.
   */
  public abstract <T> T match(
      Function<DistributionAggregationDescriptor, T> p0,
      Function<IntervalAggregationDescriptor, T> p1);

  // Seals the disjoint union.
  private AggregationDescriptor() {
  }

  /**
   * An {@link AggregationDescriptor} based on distributions.
   *
   * <p>A distribution aggregation may optionally contain a histogram of the values in the
   * population. The bucket boundaries for that histogram are described by
   * {@link getBucketBoundaries}, which defines {@code getBucketBoundaries.size() + 1 (= N)}
   * buckets. The boundaries for bucket index i are:
   * <ul>
   * <li>[-infinity, bounds[i]) for i == 0
   * <li>[bounds[i-1], bounds[i]) for 0 &lt; i &lt; N-2
   * <li>[bounds[i-1], +infinity) for i == N-1
   * </ul>
   * i.e. an underflow bucket (number 0), zero or more finite buckets (1 through N - 2, and an
   * overflow bucket (N - 1), with inclusive lower bounds and exclusive upper bounds.
   *
   * <p>Note: If N = 1, there are no finite buckets and the single bucket is both the overflow
   * and underflow bucket.
   */
  public static final class DistributionAggregationDescriptor extends AggregationDescriptor {
    /**
     * Constructs a new {@link DistributionAggregationDescriptor} with the optional
     * histogram bucket boundaries.
     */
    public static DistributionAggregationDescriptor create(List<Double> bucketBoundaries) {
      return new DistributionAggregationDescriptor(
          Collections.unmodifiableList(new ArrayList<Double>(bucketBoundaries)));
    }

    /**
     * Constructs a new {@link DistributionAggregationDescriptor} without the optional
     * histogram bucket boundaries.
     */
    public static DistributionAggregationDescriptor create() {
      return new DistributionAggregationDescriptor(null);
    }

    /**
     * The optional histogram bucket boundaries for a distribution.
     *
     * <p>Note: The returned list is unmodifiable, attempts to update it will throw an
     * UnsupportedOperationException.
     */
    @Nullable
    public List<Double> getBucketBoundaries() {
      return bucketBoundaries;
    }

    @Override
    public <T> T match(
        Function<DistributionAggregationDescriptor, T> p0,
        Function<IntervalAggregationDescriptor, T> p1) {
      return p0.apply(this);
    }

    @Nullable
    private final List<Double> bucketBoundaries;

    private DistributionAggregationDescriptor(@Nullable List<Double> bucketBoundaries) {
      this.bucketBoundaries = bucketBoundaries;
    }
  }

  /**
   * An {@link AggregationDescriptor} based on intervals.
   *
   * <p>An interval aggregation may optionally contain the sizes of each time interval.
   */
  public static final class IntervalAggregationDescriptor extends AggregationDescriptor {
    /**
     * Constructs a new {@link IntervalAggregationDescriptor} with the optional sizes of time
     * intervals.
     */
    public static IntervalAggregationDescriptor create(List<Duration> intervalSizes) {
      return new IntervalAggregationDescriptor(
          Collections.unmodifiableList(new ArrayList<Duration>(intervalSizes)));
    }

    /**
     * Constructs a new {@link IntervalAggregationDescriptor}.
     */
    public static IntervalAggregationDescriptor create() {
      return new IntervalAggregationDescriptor(null);
    }

    /**
     * The optional time intervals to record for Intervals.
     *
     * <p>Note: The returned list is unmodifiable, attempts to update it will throw an
     * UnsupportedOperationException.
     */
    @Nullable
    public List<Duration> getIntervalSizes() {
      return intervalSizes;
    }

    @Override
    public <T> T match(
        Function<DistributionAggregationDescriptor, T> p0,
        Function<IntervalAggregationDescriptor, T> p1) {
      return p1.apply(this);
    }

    @Nullable
    private final List<Duration> intervalSizes;

    private IntervalAggregationDescriptor(@Nullable List<Duration> intervalSizes) {
      this.intervalSizes = intervalSizes;
    }
  }
}
