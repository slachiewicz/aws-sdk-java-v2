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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator for all response pages in a paginated operation.
 *
 * This class is used to iterate through all the pages of an operation.
 * SDK makes service calls to retrieve the next page when next() method is called.
 *
 * The class holds a reference to the current page that is not sent to the caller. When next() method is called,
 * the current page is returned to the caller and a service call is made to get the next page.
 *
 * @param <ResponseT> The type of a single response page
 */
public class PaginatedResponsesIterator<ResponseT> implements Iterator<ResponseT> {

    private final NextPageSupplier<ResponseT> nextPageSupplier;
    private ResponseT currentResponse;

    public PaginatedResponsesIterator(ResponseT firstResponse, NextPageSupplier<ResponseT> nextPageSupplier) {
        this.currentResponse = firstResponse;
        this.nextPageSupplier = nextPageSupplier;
    }

    @Override
    public boolean hasNext() {
        return currentResponse != null;
    }

    @Override
    public ResponseT next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        ResponseT oldValue = currentResponse;

        currentResponse = nextPageSupplier.nextPage(currentResponse);

        return oldValue;
    }
}
