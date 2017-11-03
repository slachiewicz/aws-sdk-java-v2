/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.utils;

import java.time.Duration;
import software.amazon.awssdk.annotations.ReviewBeforeRelease;

public class NumericUtils {

    /**
     * Returns the {@code int} nearest in value to {@code value}.
     *
     * @param value any {@code long} value
     * @return the same value cast to {@code int} if it is in the range of the {@code int} type,
     * {@link Integer#MAX_VALUE} if it is too large, or {@link Integer#MIN_VALUE} if it is too
     * small
     */
    @ReviewBeforeRelease("Copied from Guava, confirm this is okay and copy tests too")
    public static int saturatedCast(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) value;
    }

    /**
     * Asserts that the given number is positive (non-negative and non-zero).
     *
     * @param num Number to validate
     * @param fieldName Field name to display in exception message if not positive.
     * @return Number if positive.
     */
    public static int assertIsPositive(int num, String fieldName) {
        if (num <= 0) {
            throw new IllegalArgumentException(String.format("%s must be positive", fieldName));
        }
        return num;
    }

    public static int assertIsNotNegative(int num, String fieldName) {

        if (num < 0) {
            throw new IllegalArgumentException(String.format("%s must be positive", fieldName));
        }

        return num;
    }

    /**
     * Asserts that the given duration is positive (non-negative and non-zero).
     *
     * @param duration Number to validate
     * @param fieldName Field name to display in exception message if not positive.
     * @return Duration if positive.
     */
    public static Duration assertIsPositive(Duration duration, String fieldName) {
        if (duration == null) {
            throw new IllegalArgumentException(String.format("%s cannot be null", fieldName));
        }

        if (duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException(String.format("%s must be positive", fieldName));
        }
        return duration;
    }

    /**
     * Asserts that the given duration is positive (non-negative and non-zero).
     *
     * @param duration Number to validate
     * @param fieldName Field name to display in exception message if not positive.
     * @return Duration if positive.
     */
    public static Duration assertIsNotNegative(Duration duration, String fieldName) {
        if (duration == null) {
            throw new IllegalArgumentException(String.format("%s cannot be null", fieldName));
        }

        if (duration.isNegative()) {
            throw new IllegalArgumentException(String.format("%s must be positive", fieldName));
        }

        return duration;
    }
}
