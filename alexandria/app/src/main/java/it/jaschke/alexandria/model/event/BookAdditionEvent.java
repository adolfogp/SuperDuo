/*
 * Copyright 2015 Jesús Adolfo García Pasquel
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

package it.jaschke.alexandria.model.event;

import it.jaschke.alexandria.model.domain.Book;

/**
 * Event that occurs when a book is added.
 *
 * @author Jesús Adolfo García Pasquel
 */
public class BookAdditionEvent {

    /**
     * The {@link Book} that was added.
     */
    private final Book mBook;

    /**
     * Creates a new {@link BookAdditionEvent} for the {@link Book} passed
     * as argument.
     *
     * @param movie the {@link Book} that was selected.
     */
    public BookAdditionEvent(Book movie) {
        mBook = movie;
    }

    /**
     * Returns the {@link Book} that was added.
     *
     * @return the {@link Book} that was added.
     */
    public Book getAddedBook() {
        return mBook;
    }

}
