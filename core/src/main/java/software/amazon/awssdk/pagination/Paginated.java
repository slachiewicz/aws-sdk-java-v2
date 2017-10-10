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

package software.amazon.awssdk.pagination;

/**
 * Return type interface for the paginated operations
 *
 * @param <PageT> The type of a single page
 * @param <ItemT> The type of paginated member in a response page
 */
public interface Paginated<PageT, ItemT> extends SdkIterable<PageT> {

    /**
     * @return the first response page for a paginated operation
     */
    PageT firstPage();

    /**
     * @return A {@link SdkIterable} that is used for iterating over the paginated member
     * in a response page.
     */
    SdkIterable<ItemT> allItems();
}
