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
import java.util.function.Function;
import software.amazon.awssdk.annotations.SdkProtectedApi;

/**
 * Iterable for the paginated items. This class can be used through iterate through
 * all the items across multiple pages until there is no more response from the service.
 *
 * @param <ResponseT> The type of a single response page
 * @param <ItemT> The type of paginated member in a response page
 */
@SdkProtectedApi
public class PaginatedItemsIterable<ResponseT, ItemT> implements SdkIterable<ItemT> {

    private final Paginated paginator;
    private final Function<ResponseT, Iterator<ItemT>> getItemIterator;

    public PaginatedItemsIterable(Paginated paginator,
                                  Function<ResponseT, Iterator<ItemT>> getItemIterator) {
        this.paginator = paginator;
        this.getItemIterator = getItemIterator;
    }

    @Override
    public Iterator<ItemT> iterator() {
        return new ItemIterator(paginator.iterator());
    }

    private class ItemIterator implements Iterator<ItemT> {

        private final Iterator<ResponseT> responsesIterator;
        private Iterator<ItemT> itemsIterator;

        ItemIterator(final Iterator<ResponseT> responsesIterator) {
            this.responsesIterator = responsesIterator;
            this.itemsIterator = getItemIterator.apply(responsesIterator.next());
        }

        @Override
        public boolean hasNext() {
            return (itemsIterator != null && itemsIterator.hasNext()) ||
                    responsesIterator.hasNext();
        }

        @Override
        public ItemT next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            // Using while loop here to handle response pages with empty collection of items
            while (!itemsIterator.hasNext() && responsesIterator.hasNext()) {
                itemsIterator = getItemIterator.apply(responsesIterator.next());
            }


            if (!itemsIterator.hasNext()) {
                // TODO throw error or return null
                return null;
            }

            return itemsIterator.next();
        }
    }

}
