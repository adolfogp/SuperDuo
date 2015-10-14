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

package it.jaschke.alexandria.model.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.parceler.Parcel;

/**
 * The author of a book.
 *
 * @author Jesús Adolfo García Pasquel
 */
@Parcel(Parcel.Serialization.BEAN)
public class Author {

    /**
     * The author's identifier.
     */
    private long mId;

    /**
     * The author's name.
     */
    private String mName;

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    @Override
    public int hashCode() {
        final int initial = 1459;
        final int multiplier = 151;
        return new HashCodeBuilder(initial, multiplier)
                .append(this.mId)
                .append(this.mName)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Author)) {
            return false;
        }
        Author that = ((Author) obj);
        return new EqualsBuilder()
                .append(this.mId, that.mId)
                .append(this.mName, that.mName)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("mId", this.mId)
                .append("mName", this.mName)
                .toString();
    }

}
