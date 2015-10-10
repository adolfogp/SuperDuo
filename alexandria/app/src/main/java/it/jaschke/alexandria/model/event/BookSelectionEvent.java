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
import it.jaschke.alexandria.view.fragment.ListOfBooksFragment;

/**
 * Event that occurs when a book is selected. For example, from a list of
 * books, like the one handled by {@link ListOfBooksFragment}.
 *
 * @author Jesús Adolfo García Pasquel
 */
public class BookSelectionEvent {

    /**
     * The {@link Book} that was selected.
     */
    private final Book mBook;

    /**
     * Creates a new {@link BookSelectionEvent} for the {@link Book} passed
     * as argument.
     *
     * @param movie the {@link Book} that was selected.
     */
    public BookSelectionEvent(Book movie) {
        mBook = movie;
    }

    /**
     * Returns the selected {@link Book}.
     *
     * @return the selected {@link Book}.
     */
    public Book getSelectedBook() {
        return mBook;
    }

}
